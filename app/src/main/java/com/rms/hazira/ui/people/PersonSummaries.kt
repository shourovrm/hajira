package com.rms.hazira.ui.people

import com.rms.hazira.domain.Person
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.formatTaka
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** The week as Bangladesh counts it, starting on Saturday. */
val weekdaysStartingSaturday: List<DayOfWeek> = listOf(
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
)

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

/** 16:30 becomes "4:30 pm". */
fun formatExpectedTime(time: LocalTime): String {
    return time.format(timeFormatter).lowercase(Locale.ENGLISH)
}

/** "Sat to Thu, 4:30 pm", "Every day" or "Sun, Tue, Thu". */
fun summariseSchedule(person: Person): String {
    val daysText = summariseWeekdays(person.scheduledWeekdays)
    val time = person.expectedTime
    if (time == null) {
        return daysText
    }
    return daysText + ", " + formatExpectedTime(time)
}

private fun summariseWeekdays(weekdays: Set<DayOfWeek>): String {
    if (weekdays.isEmpty()) {
        return "No fixed days"
    }
    if (weekdays.size == 7) {
        return "Every day"
    }
    val orderedWeekdays = weekdaysStartingSaturday.filter { weekday -> weekday in weekdays }
    if (orderedWeekdays.size >= 3 && isUnbrokenRun(orderedWeekdays)) {
        return shortName(orderedWeekdays.first()) + " to " + shortName(orderedWeekdays.last())
    }
    return orderedWeekdays.joinToString(separator = ", ") { weekday -> shortName(weekday) }
}

private fun isUnbrokenRun(orderedWeekdays: List<DayOfWeek>): Boolean {
    val firstPosition = weekdaysStartingSaturday.indexOf(orderedWeekdays.first())
    val lastPosition = weekdaysStartingSaturday.indexOf(orderedWeekdays.last())
    return lastPosition - firstPosition + 1 == orderedWeekdays.size
}

fun shortName(weekday: DayOfWeek): String {
    return weekday.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
}

/** "৳3,000 a month", "৳500 a visit" or "৳90 a litre". */
fun describeRate(person: Person): String {
    val amount = formatTaka(person.rateTaka)
    return when (person.rateKind) {
        RateKind.MONTHLY -> "$amount a month"
        RateKind.PER_VISIT -> "$amount a visit"
        RateKind.PER_UNIT -> amount + " a " + person.unitName.ifBlank { "unit" }
    }
}
