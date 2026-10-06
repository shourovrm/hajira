package com.rms.hazira.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

val september2026: YearMonth = YearMonth.of(2026, 9)

fun testPerson(
    id: Long = 1,
    name: String = "Sumon Sir",
    kind: ServiceKind = ServiceKind.VISIT,
    rateKind: RateKind = RateKind.PER_VISIT,
    rateTaka: Int = 500,
    unitName: String = "",
    scheduledWeekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
): Person {
    return Person(
        id = id,
        name = name,
        role = "Tutor",
        kind = kind,
        scheduledWeekdays = scheduledWeekdays,
        expectedTime = null,
        rateKind = rateKind,
        rateTaka = rateTaka,
        unitName = unitName,
    )
}

fun cameRecord(personId: Long, date: LocalDate, quantity: Double = 1.0): DayRecord {
    return DayRecord(personId = personId, date = date, status = DayStatus.CAME, quantity = quantity)
}

fun septemberDate(dayOfMonth: Int): LocalDate {
    return september2026.atDay(dayOfMonth)
}

/** One CAME record per listed day of September 2026. */
fun cameRecordsOn(personId: Long, daysOfMonth: List<Int>): List<DayRecord> {
    return daysOfMonth.map { dayOfMonth -> cameRecord(personId, septemberDate(dayOfMonth)) }
}

fun testPayment(
    personId: Long = 1,
    month: YearMonth = september2026,
    amountTaka: Int,
    paidOn: LocalDate = septemberDate(12),
): Payment {
    return Payment(personId = personId, month = month, amountTaka = amountTaka, paidOn = paidOn)
}
