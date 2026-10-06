package com.rms.hazira.ui.today

import androidx.compose.ui.graphics.Color
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.formatQuantityWithUnit
import com.rms.hazira.ui.common.statusWord
import com.rms.hazira.ui.theme.HaziraColours
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// The interface text is English whatever the phone's language, so dates are English too.
private val longDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)

/** "Wednesday, 7 October". */
fun formatLongDate(date: LocalDate): String {
    return longDateFormatter.format(date)
}

/** "Saturday". */
fun formatWeekdayName(weekday: DayOfWeek): String {
    return weekday.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}

/** What happened on a marked day, in words: "Came", or "1 litre" for a delivery that arrived. */
fun outcomeWords(person: Person, record: DayRecord): String {
    val isDeliveryThatArrived = person.kind == ServiceKind.DELIVERY && record.status == DayStatus.CAME
    if (isDeliveryThatArrived) {
        return formatQuantityWithUnit(record.quantity, person.unitName)
    }
    return statusWord(record.status, person.kind)
}

fun outcomeColour(status: DayStatus): Color {
    return when (status) {
        DayStatus.CAME -> HaziraColours.CoverGreen
        DayStatus.ABSENT -> HaziraColours.AbsentRed
        DayStatus.HOLIDAY -> HaziraColours.MutedText
    }
}
