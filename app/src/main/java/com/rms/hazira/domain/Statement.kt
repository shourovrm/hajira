package com.rms.hazira.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val paymentDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

/** "September 2026". */
fun formatMonthTitle(month: YearMonth): String {
    return month.format(monthTitleFormatter)
}

/** "7 Oct 2026". */
fun formatPaymentDate(date: LocalDate): String {
    return date.format(paymentDateFormatter)
}

/**
 * How the charge was worked out, without the total: "13 classes at ৳500", "32 litre at ৳90"
 * or "Monthly fee ৳3,000".
 */
fun describeCharge(account: MonthAccount): String {
    val person = account.person
    val rate = formatTaka(person.rateTaka)
    return when (person.rateKind) {
        RateKind.MONTHLY -> "Monthly fee $rate"
        RateKind.PER_VISIT -> "${account.cameDays} ${visitWord(person.kind, account.cameDays)} at $rate"
        RateKind.PER_UNIT -> "${formatQuantityWithUnit(account.quantityTotal, person.unitName)} at $rate"
    }
}

private fun visitWord(kind: ServiceKind, count: Int): String {
    if (kind == ServiceKind.OUTING) {
        return if (count == 1) "class" else "classes"
    }
    return if (count == 1) "visit" else "visits"
}

/**
 * The plain text a household sends to the person, for example on WhatsApp, so both sides
 * see the same count. [records] and [payments] may hold other people and months.
 */
fun buildStatementText(account: MonthAccount, records: List<DayRecord>, payments: List<Payment>): String {
    val person = account.person
    val lines = mutableListOf<String>()
    lines.add("${person.name}, ${formatMonthTitle(account.month)}")
    lines.add("")

    val recordsInMonth = records.filter { record ->
        record.personId == person.id && YearMonth.from(record.date) == account.month
    }
    lines.addAll(attendanceLines(person, recordsInMonth))

    if (person.kind == ServiceKind.DELIVERY) {
        lines.add("Total: ${formatQuantityWithUnit(account.quantityTotal, person.unitName)}")
    }
    lines.add("")

    lines.add(chargeLine(account))
    val monthPayments = payments
        .filter { payment -> payment.personId == person.id && payment.month == account.month }
        .sortedBy { payment -> payment.paidOn }
    for (payment in monthPayments) {
        lines.add("Paid on ${formatPaymentDate(payment.paidOn)}: ${formatTaka(payment.amountTaka)}")
    }
    lines.add(dueLine(account))

    return lines.joinToString(separator = "\n")
}

private fun attendanceLines(person: Person, recordsInMonth: List<DayRecord>): List<String> {
    val lines = mutableListOf<String>()
    val cameLabel = if (person.kind == ServiceKind.DELIVERY) "Delivered on days" else "Came on days"
    val cameDays = dayNumbers(recordsInMonth, DayStatus.CAME)
    if (cameDays.isEmpty()) {
        lines.add("$cameLabel: none")
    } else {
        lines.add("$cameLabel: $cameDays")
    }
    val absentDays = dayNumbers(recordsInMonth, DayStatus.ABSENT)
    if (absentDays.isNotEmpty()) {
        lines.add("Absent on days: $absentDays")
    }
    return lines
}

private fun dayNumbers(recordsInMonth: List<DayRecord>, status: DayStatus): String {
    return recordsInMonth
        .filter { record -> record.status == status }
        .map { record -> record.date.dayOfMonth }
        .sorted()
        .joinToString(separator = ", ")
}

private fun chargeLine(account: MonthAccount): String {
    if (account.person.rateKind == RateKind.MONTHLY) {
        if (account.chargeTaka == 0) {
            return "Monthly fee: nothing to charge, no day was recorded"
        }
        return describeCharge(account)
    }
    return "${describeCharge(account)} = ${formatTaka(account.chargeTaka)}"
}

private fun dueLine(account: MonthAccount): String {
    if (account.dueTaka > 0) {
        return "Still to pay: ${formatTaka(account.dueTaka)}"
    }
    if (account.dueTaka == 0) {
        return "Nothing left to pay"
    }
    return "Paid ${formatTaka(-account.dueTaka)} more than the charge"
}
