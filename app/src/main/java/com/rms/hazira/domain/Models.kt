package com.rms.hazira.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/** How the household meets the service. It decides the wording and whether a quantity is asked for. */
enum class ServiceKind {
    /** Someone comes to the house: a teacher, a tutor, house help. */
    VISIT,

    /** A household member goes out to it: a coaching class, an art school. */
    OUTING,

    /** Something is delivered and the amount can change from day to day: milk, water, eggs. */
    DELIVERY,
}

/** How the month's charge is worked out. */
enum class RateKind {
    /** One fixed amount for the month, whatever the attendance. */
    MONTHLY,

    /** An amount for each day marked as came. */
    PER_VISIT,

    /** An amount for each unit delivered, for example each litre. */
    PER_UNIT,
}

enum class DayStatus {
    CAME,
    ABSENT,

    /** An agreed day off. It is neither an attendance nor an absence. */
    HOLIDAY,
}

/**
 * One recurring service the household tracks. Usually a person, sometimes a place such as an art school.
 *
 * @property id 0 means "not saved yet"; the database assigns the real id.
 * @property role Free text shown under the name, for example "Quran teacher".
 * @property scheduledWeekdays The weekdays this service is expected on.
 * @property expectedTime Used to order the Today list. Null when there is no fixed time.
 * @property rateTaka Whole taka. What it is charged for depends on [rateKind].
 * @property unitName What a quantity is counted in, for example "litre". Only used for deliveries.
 * @property defaultQuantity The amount pre-filled when a delivery is marked.
 * @property colourIndex Index into the fixed palette in `ui/common/PersonColours.kt`.
 * @property isActive False once the service has stopped. Its history is kept.
 */
data class Person(
    val id: Long = 0,
    val name: String,
    val role: String,
    val kind: ServiceKind,
    val scheduledWeekdays: Set<DayOfWeek>,
    val expectedTime: LocalTime?,
    val rateKind: RateKind,
    val rateTaka: Int,
    val unitName: String = "",
    val defaultQuantity: Double = 1.0,
    val phone: String = "",
    val colourIndex: Int = 0,
    val isActive: Boolean = true,
)

/**
 * What happened with one person on one day. There is at most one record per person per date;
 * no record means the day has not been marked.
 *
 * @property quantity Units delivered. 1.0 for visits and outings, where it carries no meaning.
 */
data class DayRecord(
    val personId: Long,
    val date: LocalDate,
    val status: DayStatus,
    val quantity: Double = 1.0,
    val note: String = "",
)

/**
 * Money given to a person towards one month's charge. An advance is simply a payment made
 * before the month is over.
 *
 * @property id 0 means "not saved yet".
 * @property month The month the money counts towards, which can differ from the month of [paidOn].
 */
data class Payment(
    val id: Long = 0,
    val personId: Long,
    val month: YearMonth,
    val amountTaka: Int,
    val paidOn: LocalDate,
    val note: String = "",
)
