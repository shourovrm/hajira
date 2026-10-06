package com.rms.hazira.ui.month

import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.formatQuantity
import com.rms.hazira.domain.formatQuantityWithUnit

/** "25 days" or "32 litre": came days, or the delivered total for a delivery. [personRecords] are one person's month. */
fun monthCountText(person: Person, personRecords: List<DayRecord>): String {
    val cameRecords = personRecords.filter { record -> record.status == DayStatus.CAME }
    if (person.kind == ServiceKind.DELIVERY) {
        return formatQuantityWithUnit(cameRecords.sumOf { record -> record.quantity }, person.unitName)
    }
    if (cameRecords.size == 1) {
        return "1 day"
    }
    return "${cameRecords.size} days"
}

/** The same count without a unit, for the narrow total column of the register. */
fun monthCountNumber(person: Person, personRecords: List<DayRecord>): String {
    val cameRecords = personRecords.filter { record -> record.status == DayStatus.CAME }
    if (person.kind == ServiceKind.DELIVERY) {
        return formatQuantity(cameRecords.sumOf { record -> record.quantity })
    }
    return cameRecords.size.toString()
}
