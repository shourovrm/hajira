package com.rms.hazira.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.ServiceKind
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val role: String,
    /** [ServiceKind] name. */
    val kind: String,
    /** ISO day numbers joined by commas, for example "1,3,5" for Monday, Wednesday, Friday. */
    val scheduledWeekdays: String,
    /** Seconds since midnight, or null when there is no fixed time. */
    val expectedTimeSecondOfDay: Int?,
    /** [RateKind] name. */
    val rateKind: String,
    val rateTaka: Int,
    val unitName: String,
    val defaultQuantity: Double,
    val phone: String,
    val colourIndex: Int,
    val isActive: Boolean,
)

fun PersonEntity.toDomain(): Person {
    return Person(
        id = id,
        name = name,
        role = role,
        kind = ServiceKind.valueOf(kind),
        scheduledWeekdays = decodeWeekdays(scheduledWeekdays),
        expectedTime = expectedTimeSecondOfDay?.let { secondOfDay -> LocalTime.ofSecondOfDay(secondOfDay.toLong()) },
        rateKind = RateKind.valueOf(rateKind),
        rateTaka = rateTaka,
        unitName = unitName,
        defaultQuantity = defaultQuantity,
        phone = phone,
        colourIndex = colourIndex,
        isActive = isActive,
    )
}

fun Person.toEntity(): PersonEntity {
    return PersonEntity(
        id = id,
        name = name,
        role = role,
        kind = kind.name,
        scheduledWeekdays = encodeWeekdays(scheduledWeekdays),
        expectedTimeSecondOfDay = expectedTime?.toSecondOfDay(),
        rateKind = rateKind.name,
        rateTaka = rateTaka,
        unitName = unitName,
        defaultQuantity = defaultQuantity,
        phone = phone,
        colourIndex = colourIndex,
        isActive = isActive,
    )
}

private fun encodeWeekdays(weekdays: Set<DayOfWeek>): String {
    return weekdays.map { weekday -> weekday.value }.sorted().joinToString(separator = ",")
}

private fun decodeWeekdays(encoded: String): Set<DayOfWeek> {
    if (encoded.isBlank()) {
        return emptySet()
    }
    return encoded.split(",").map { dayNumber -> DayOfWeek.of(dayNumber.trim().toInt()) }.toSet()
}
