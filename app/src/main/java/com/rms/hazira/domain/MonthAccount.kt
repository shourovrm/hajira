package com.rms.hazira.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/** One person's attendance and money for one month. Built by [buildMonthAccount]. */
data class MonthAccount(
    val person: Person,
    val month: YearMonth,
    /** Days in the month that fall on the person's scheduled weekdays. */
    val scheduledDays: Int,
    val cameDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    /** Sum of the quantities on the days marked as came. */
    val quantityTotal: Double,
    /** Weeks charged to this month. Only counted for a weekly rate; 0 otherwise. */
    val chargedWeeks: Int,
    val chargeTaka: Int,
    val paidTaka: Int,
) {
    /** Negative when the household has paid more than the charge. */
    val dueTaka: Int
        get() = chargeTaka - paidTaka

    val isSettled: Boolean
        get() = chargeTaka > 0 && dueTaka <= 0
}

/** True when [date] falls on one of the person's scheduled weekdays. */
fun isDueOn(person: Person, date: LocalDate): Boolean {
    return date.dayOfWeek in person.scheduledWeekdays
}

/**
 * Works out one person's month from their records and payments.
 *
 * [records] and [payments] may hold other people's rows and other months; they are filtered here
 * so callers can pass whatever the repository gave them.
 *
 * For a weekly rate, [records] must reach back to [firstDateNeededFor] the month. A week that
 * straddles two months is charged once, to the month of its first came day, and deciding that
 * needs the last days of the month before.
 */
fun buildMonthAccount(
    person: Person,
    month: YearMonth,
    records: List<DayRecord>,
    payments: List<Payment>,
): MonthAccount {
    val recordsInMonth = records.filter { record ->
        record.personId == person.id && YearMonth.from(record.date) == month
    }
    val cameRecords = recordsInMonth.filter { record -> record.status == DayStatus.CAME }
    val quantityTotal = cameRecords.sumOf { record -> record.quantity }
    val chargedWeeks = if (person.rateKind == RateKind.WEEKLY) countChargedWeeks(person, month, records) else 0

    val paidTaka = payments
        .filter { payment -> payment.personId == person.id && payment.month == month }
        .sumOf { payment -> payment.amountTaka }

    return MonthAccount(
        person = person,
        month = month,
        scheduledDays = countScheduledDays(person, month),
        cameDays = cameRecords.size,
        absentDays = recordsInMonth.count { record -> record.status == DayStatus.ABSENT },
        holidayDays = recordsInMonth.count { record -> record.status == DayStatus.HOLIDAY },
        quantityTotal = quantityTotal,
        chargedWeeks = chargedWeeks,
        chargeTaka = chargeFor(person, cameRecords.size, quantityTotal, chargedWeeks),
        paidTaka = paidTaka,
    )
}

private fun countScheduledDays(person: Person, month: YearMonth): Int {
    var scheduledDays = 0
    for (dayOfMonth in 1..month.lengthOfMonth()) {
        if (isDueOn(person, month.atDay(dayOfMonth))) {
            scheduledDays += 1
        }
    }
    return scheduledDays
}

/** The earliest date whose records [buildMonthAccount] needs to work out [month]. */
fun firstDateNeededFor(month: YearMonth): LocalDate {
    return weekStartOf(month.atDay(1))
}

/** The Saturday that starts the week [date] is in. The week runs Saturday to Friday in Bangladesh. */
private fun weekStartOf(date: LocalDate): LocalDate {
    return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
}

/** Counts the weeks whose first came day falls in [month]. */
private fun countChargedWeeks(person: Person, month: YearMonth, records: List<DayRecord>): Int {
    val firstCameDayOfEachWeek = mutableMapOf<LocalDate, LocalDate>()
    for (record in records) {
        if (record.personId != person.id || record.status != DayStatus.CAME) {
            continue
        }
        val weekStart = weekStartOf(record.date)
        val earliestSoFar = firstCameDayOfEachWeek[weekStart]
        if (earliestSoFar == null || record.date.isBefore(earliestSoFar)) {
            firstCameDayOfEachWeek[weekStart] = record.date
        }
    }
    return firstCameDayOfEachWeek.values.count { firstCameDay -> YearMonth.from(firstCameDay) == month }
}

private fun chargeFor(person: Person, cameDays: Int, quantityTotal: Double, chargedWeeks: Int): Int {
    return when (person.rateKind) {
        // A monthly fee is owed only for a month the service actually ran in. Without this,
        // every month before the person was added would show the full fee as unpaid.
        RateKind.MONTHLY -> if (cameDays > 0) person.rateTaka else 0
        RateKind.WEEKLY -> chargedWeeks * person.rateTaka
        RateKind.PER_VISIT -> cameDays * person.rateTaka
        // Half a litre at an odd price gives a fraction of a taka; nobody settles in poisha.
        RateKind.PER_UNIT -> (quantityTotal * person.rateTaka).roundToInt()
    }
}
