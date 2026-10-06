package com.rms.hazira.ui.common

import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.ServiceKind

/**
 * The word for a day's status. It depends on the kind of service because a tutor "came",
 * a child "went" to class, and milk was "delivered".
 */
fun statusWord(status: DayStatus, kind: ServiceKind): String {
    if (status == DayStatus.HOLIDAY) {
        return "Day off"
    }
    return when (kind) {
        ServiceKind.VISIT -> if (status == DayStatus.CAME) "Came" else "Absent"
        ServiceKind.OUTING -> if (status == DayStatus.CAME) "Went" else "Missed"
        ServiceKind.DELIVERY -> if (status == DayStatus.CAME) "Delivered" else "No delivery"
    }
}
