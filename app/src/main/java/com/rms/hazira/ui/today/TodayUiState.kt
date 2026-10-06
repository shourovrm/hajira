package com.rms.hazira.ui.today

import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.isDueOn
import java.time.LocalDate

data class MarkedEntry(val person: Person, val record: DayRecord)

/**
 * Everything the Today tab draws.
 *
 * @property today The real current date, as last read when the screen resumed.
 * @property shownDate The date being marked. It is [today] unless the user stepped back.
 * @property people Active people only.
 * @property records The records of [shownDate].
 * @property isLoaded False until the first read of the database finishes, so the empty state
 *   is not flashed at people who do have data.
 */
data class TodayUiState(
    val today: LocalDate,
    val shownDate: LocalDate,
    val people: List<Person>,
    val records: List<DayRecord>,
    val isLoaded: Boolean,
) {
    val isShowingToday: Boolean
        get() = shownDate == today

    private val recordByPersonId: Map<Long, DayRecord>
        get() = records.associateBy { record -> record.personId }

    /** People due on the shown date with no record yet, earliest expected time first. */
    val stillToMark: List<Person>
        get() {
            val recordedIds = recordByPersonId
            val dueAndUnmarked = people.filter { person ->
                isDueOn(person, shownDate) && person.id !in recordedIds
            }
            val (withTime, withoutTime) = dueAndUnmarked.partition { person -> person.expectedTime != null }
            return withTime.sortedBy { person -> person.expectedTime } + withoutTime
        }

    /** Everyone with a record on the shown date, including extra visits on days they are not due. */
    val marked: List<MarkedEntry>
        get() {
            val recordedIds = recordByPersonId
            return people.mapNotNull { person ->
                val record = recordedIds[person.id]
                if (record == null) null else MarkedEntry(person, record)
            }
        }

    /** People who are not due on the shown date and have no record. */
    val notDue: List<Person>
        get() {
            val recordedIds = recordByPersonId
            return people.filter { person ->
                !isDueOn(person, shownDate) && person.id !in recordedIds
            }
        }
}
