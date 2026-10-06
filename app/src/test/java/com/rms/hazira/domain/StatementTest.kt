package com.rms.hazira.domain

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatementTest {

    private val thirteenDays = listOf(1, 3, 6, 8, 10, 13, 17, 20, 22, 24, 27, 28, 29)

    @Test
    fun tutorStatementListsDaysChargePaymentAndAmountStillToPay() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, thirteenDays) +
            DayRecord(tutor.id, septemberDate(15), DayStatus.ABSENT)
        val payments = listOf(testPayment(amountTaka = 2_000, paidOn = septemberDate(12)))
        val account = buildMonthAccount(tutor, september2026, records, payments)

        val text = buildStatementText(account, records, payments)

        val expected = listOf(
            "Sumon Sir, September 2026",
            "",
            "Came on days: 1, 3, 6, 8, 10, 13, 17, 20, 22, 24, 27, 28, 29",
            "Absent on days: 15",
            "",
            "13 visits at ৳500 = ৳6,500",
            "Paid on 12 Sep 2026: ৳2,000",
            "Still to pay: ৳4,500",
        ).joinToString("\n")
        assertEquals(expected, text)
    }

    @Test
    fun outingChargeSaysClasses() {
        val school = testPerson(kind = ServiceKind.OUTING)
        val records = cameRecordsOn(school.id, thirteenDays)
        val account = buildMonthAccount(school, september2026, records, emptyList())

        val text = buildStatementText(account, records, emptyList())

        assertTrue(text.contains("13 classes at ৳500 = ৳6,500"))
    }

    @Test
    fun deliveryStatementShowsQuantityTotalAndCharge() {
        val milk = testPerson(
            name = "Milkman",
            kind = ServiceKind.DELIVERY,
            rateKind = RateKind.PER_UNIT,
            rateTaka = 90,
            unitName = "litre",
        )
        val records = (1..16).map { dayOfMonth -> cameRecord(milk.id, septemberDate(dayOfMonth), quantity = 2.0) }
        val account = buildMonthAccount(milk, september2026, records, emptyList())

        val text = buildStatementText(account, records, emptyList())

        assertTrue(text.contains("Total: 32 litre"))
        assertTrue(text.contains("32 litre at ৳90 = ৳2,880"))
        assertTrue(text.contains("Still to pay: ৳2,880"))
    }

    @Test
    fun monthlyStatementShowsFeeWithoutMultiplication() {
        val teacher = testPerson(rateKind = RateKind.MONTHLY, rateTaka = 3_000)
        val records = cameRecordsOn(teacher.id, listOf(1, 2))
        val account = buildMonthAccount(teacher, september2026, records, emptyList())

        val text = buildStatementText(account, records, emptyList())

        assertTrue(text.contains("Monthly fee ৳3,000"))
        assertFalse(text.contains("="))
    }

    @Test
    fun absentLineIsLeftOutWhenNoAbsence() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, listOf(1))
        val account = buildMonthAccount(tutor, september2026, records, emptyList())

        val text = buildStatementText(account, records, emptyList())

        assertFalse(text.contains("Absent"))
    }

    @Test
    fun settledMonthSaysNothingLeftToPay() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, listOf(1, 2))
        val payments = listOf(testPayment(amountTaka = 1_000))
        val account = buildMonthAccount(tutor, september2026, records, payments)

        val text = buildStatementText(account, records, payments)

        assertTrue(text.endsWith("Nothing left to pay"))
    }

    @Test
    fun otherPeopleAndMonthsAreLeftOut() {
        val tutor = testPerson(id = 1)
        val records = cameRecordsOn(1, listOf(1)) +
            cameRecordsOn(2, listOf(2)) +
            cameRecord(1, YearMonth.of(2026, 8).atDay(30))
        val payments = listOf(
            testPayment(personId = 2, amountTaka = 999),
            testPayment(month = YearMonth.of(2026, 8), amountTaka = 888),
        )
        val account = buildMonthAccount(tutor, september2026, records, payments)

        val text = buildStatementText(account, records, payments)

        assertTrue(text.contains("Came on days: 1\n"))
        assertFalse(text.contains("999"))
        assertFalse(text.contains("888"))
    }

    @Test
    fun paymentsAreListedOldestFirst() {
        val tutor = testPerson()
        val records = cameRecordsOn(tutor.id, listOf(1, 2, 3, 4))
        val payments = listOf(
            testPayment(amountTaka = 700, paidOn = septemberDate(20)),
            testPayment(amountTaka = 300, paidOn = septemberDate(5)),
        )
        val account = buildMonthAccount(tutor, september2026, records, payments)

        val text = buildStatementText(account, records, payments)

        val earlierPosition = text.indexOf("5 Sep 2026")
        val laterPosition = text.indexOf("20 Sep 2026")
        assertTrue(earlierPosition in 0 until laterPosition)
    }
}
