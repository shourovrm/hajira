package com.rms.hazira.ui.dues

import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.MonthAccount
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.describeCharge
import com.rms.hazira.ui.common.statusWord

/** The one-line reason under a name: "13 classes at ৳500", "Came 25 of 26 days" or "Monthly fee". */
fun describeDueReason(account: MonthAccount): String {
    if (account.chargeTaka == 0) {
        return "Payment recorded"
    }
    if (account.person.rateKind != RateKind.MONTHLY) {
        return describeCharge(account)
    }
    if (account.scheduledDays == 0) {
        return "Monthly fee"
    }
    val cameWord = statusWord(DayStatus.CAME, account.person.kind)
    // A make-up day outside the schedule can push the count above the scheduled days.
    if (account.cameDays > account.scheduledDays) {
        val dayWord = if (account.cameDays == 1) "day" else "days"
        return "$cameWord ${account.cameDays} $dayWord"
    }
    return "$cameWord ${account.cameDays} of ${account.scheduledDays} days"
}
