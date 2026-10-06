package com.rms.hazira.domain

import java.time.DayOfWeek
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthAccountTest {

    private val thirteenDays = listOf(1, 3, 6, 8, 10, 13, 17, 20, 22, 24, 27, 28, 29)

    @Test
    fun perVisitChargeIsCameDaysTimesRate() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, thirteenDays)

        val account = buildMonthAccount(tutor, september2026, records, emptyList())

        assertEquals(13, account.cameDays)
        assertEquals(6_500, account.chargeTaka)
    }

    @Test
    fun paymentReducesAmountStillDue() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, thirteenDays)
        val payments = listOf(testPayment(amountTaka = 2_000))

        val account = buildMonthAccount(tutor, september2026, records, payments)

        assertEquals(2_000, account.paidTaka)
        assertEquals(4_500, account.dueTaka)
        assertFalse(account.isSettled)
    }

    @Test
    fun perUnitChargeIsQuantityTotalTimesRate() {
        val milk = testPerson(
            name = "Milkman",
            kind = ServiceKind.DELIVERY,
            rateKind = RateKind.PER_UNIT,
            rateTaka = 90,
            unitName = "litre",
        )
        val records = (1..16).map { dayOfMonth -> cameRecord(milk.id, septemberDate(dayOfMonth), quantity = 2.0) }

        val account = buildMonthAccount(milk, september2026, records, emptyList())

        assertEquals(32.0, account.quantityTotal, 0.0)
        assertEquals(2_880, account.chargeTaka)
    }

    @Test
    fun perUnitChargeRoundsHalfTakaUp() {
        val milk = testPerson(kind = ServiceKind.DELIVERY, rateKind = RateKind.PER_UNIT, rateTaka = 45)
        val records = listOf(cameRecord(milk.id, septemberDate(1), quantity = 0.5))

        val account = buildMonthAccount(milk, september2026, records, emptyList())

        assertEquals(23, account.chargeTaka)
    }

    @Test
    fun perUnitHalfLitresAddUp() {
        val milk = testPerson(kind = ServiceKind.DELIVERY, rateKind = RateKind.PER_UNIT, rateTaka = 90)
        val records = listOf(
            cameRecord(milk.id, septemberDate(1), quantity = 1.5),
            cameRecord(milk.id, septemberDate(2), quantity = 0.5),
        )

        val account = buildMonthAccount(milk, september2026, records, emptyList())

        assertEquals(180, account.chargeTaka)
    }

    @Test
    fun monthlyChargeIsFullRateWhenAnyDayCame() {
        val teacher = testPerson(rateKind = RateKind.MONTHLY, rateTaka = 3_000)
        val records = cameRecordsOn(teacher.id, listOf(5))

        val account = buildMonthAccount(teacher, september2026, records, emptyList())

        assertEquals(3_000, account.chargeTaka)
    }

    @Test
    fun monthlyChargeIsZeroWhenNoDayCame() {
        val teacher = testPerson(rateKind = RateKind.MONTHLY, rateTaka = 3_000)
        val absentOnly = listOf(DayRecord(teacher.id, septemberDate(5), DayStatus.ABSENT))

        val account = buildMonthAccount(teacher, september2026, absentOnly, emptyList())

        assertEquals(0, account.chargeTaka)
        assertEquals(1, account.absentDays)
    }

    @Test
    fun monthlyChargeIsZeroWithNoRecordsAtAll() {
        val teacher = testPerson(rateKind = RateKind.MONTHLY, rateTaka = 3_000)

        val account = buildMonthAccount(teacher, september2026, emptyList(), emptyList())

        assertEquals(0, account.chargeTaka)
        assertFalse(account.isSettled)
    }

    @Test
    fun holidaysAreCountedButNotCharged() {
        val tutor = testPerson()
        val records = listOf(
            cameRecord(tutor.id, septemberDate(1)),
            DayRecord(tutor.id, septemberDate(2), DayStatus.HOLIDAY),
        )

        val account = buildMonthAccount(tutor, september2026, records, emptyList())

        assertEquals(1, account.holidayDays)
        assertEquals(500, account.chargeTaka)
    }

    @Test
    fun recordsAndPaymentsOfOtherPeopleAreIgnored() {
        val tutor = testPerson(id = 1)
        val records = cameRecordsOn(personId = 1, daysOfMonth = listOf(1, 2)) +
            cameRecordsOn(personId = 2, daysOfMonth = listOf(3, 4, 5))
        val payments = listOf(
            testPayment(personId = 1, amountTaka = 300),
            testPayment(personId = 2, amountTaka = 900),
        )

        val account = buildMonthAccount(tutor, september2026, records, payments)

        assertEquals(2, account.cameDays)
        assertEquals(1_000, account.chargeTaka)
        assertEquals(300, account.paidTaka)
    }

    @Test
    fun recordsAndPaymentsOfOtherMonthsAreIgnored() {
        val tutor = testPerson()
        val records = listOf(
            cameRecord(tutor.id, septemberDate(30)),
            cameRecord(tutor.id, YearMonth.of(2026, 8).atDay(31)),
            cameRecord(tutor.id, YearMonth.of(2026, 10).atDay(1)),
        )
        val payments = listOf(
            testPayment(amountTaka = 100),
            testPayment(month = YearMonth.of(2026, 8), amountTaka = 700),
        )

        val account = buildMonthAccount(tutor, september2026, records, payments)

        assertEquals(1, account.cameDays)
        assertEquals(100, account.paidTaka)
    }

    @Test
    fun overpaymentGivesNegativeDueAndIsSettled() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, listOf(1))
        val payments = listOf(testPayment(amountTaka = 800))

        val account = buildMonthAccount(tutor, september2026, records, payments)

        assertEquals(-300, account.dueTaka)
        assertTrue(account.isSettled)
    }

    @Test
    fun scheduledDaysCountsMatchingWeekdaysInMonth() {
        // September 2026 has four Saturdays (5, 12, 19, 26).
        val saturdayOnly = testPerson(scheduledWeekdays = setOf(DayOfWeek.SATURDAY))

        val account = buildMonthAccount(saturdayOnly, september2026, emptyList(), emptyList())

        assertEquals(4, account.scheduledDays)
    }

    @Test
    fun isDueOnMatchesScheduledWeekdaysOnly() {
        val saturdayOnly = testPerson(scheduledWeekdays = setOf(DayOfWeek.SATURDAY))

        assertTrue(isDueOn(saturdayOnly, septemberDate(5)))
        assertFalse(isDueOn(saturdayOnly, septemberDate(6)))
    }

    @Test
    fun isDueOnIsFalseWithNoScheduledWeekdays() {
        val unscheduled = testPerson(scheduledWeekdays = emptySet())

        assertFalse(isDueOn(unscheduled, septemberDate(5)))
    }

    @Test
    fun weeklyChargeCountsWeeksWithACameDay() {
        val helper = testPerson(rateKind = RateKind.WEEKLY, rateTaka = 1_200)
        // 7 and 8 September are in the same Saturday-to-Friday week, so they count once.
        val records = cameRecordsOn(helper.id, listOf(7, 8, 15, 22))

        val account = buildMonthAccount(helper, september2026, records, emptyList())

        assertEquals(3, account.chargedWeeks)
        assertEquals(3_600, account.chargeTaka)
    }

    @Test
    fun weekAcrossTwoMonthsIsChargedOnceToTheMonthOfItsFirstCameDay() {
        val helper = testPerson(rateKind = RateKind.WEEKLY, rateTaka = 1_200)
        val october2026 = YearMonth.of(2026, 10)
        // Saturday 26 September to Friday 2 October is one week.
        val records = listOf(
            cameRecord(helper.id, septemberDate(30)),
            cameRecord(helper.id, october2026.atDay(1)),
        )

        val septemberAccount = buildMonthAccount(helper, september2026, records, emptyList())
        val octoberAccount = buildMonthAccount(helper, october2026, records, emptyList())

        assertEquals(1_200, septemberAccount.chargeTaka)
        assertEquals(0, octoberAccount.chargeTaka)
    }

    @Test
    fun weekThatStartedInThePreviousMonthIsNotChargedAgain() {
        val helper = testPerson(rateKind = RateKind.WEEKLY, rateTaka = 1_200)
        val august2026 = YearMonth.of(2026, 8)
        val records = listOf(
            cameRecord(helper.id, august2026.atDay(31)),
            cameRecord(helper.id, septemberDate(1)),
        )

        val septemberAccount = buildMonthAccount(helper, september2026, records, emptyList())

        assertEquals(0, septemberAccount.chargeTaka)
        // The Dues screen relies on this date to load the August days the rule above needs.
        assertEquals(august2026.atDay(29), firstDateNeededFor(september2026))
    }
}
