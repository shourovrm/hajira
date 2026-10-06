package com.rms.hazira.ui.month

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rms.hazira.HaziraApp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.Person
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Key for looking up one person's record on one day. */
data class PersonDay(val personId: Long, val date: LocalDate)

/**
 * Everything the Month tab draws.
 *
 * @property people Active people, plus stopped people who have a record in [month].
 * @property records Every record in [month].
 */
data class MonthUiState(
    val month: YearMonth,
    val today: LocalDate,
    val people: List<Person>,
    val records: List<DayRecord>,
    val isLoaded: Boolean,
) {
    val recordsByPersonDay: Map<PersonDay, DayRecord> =
        records.associateBy { record -> PersonDay(record.personId, record.date) }

    /** The next month would be in the future. */
    val isCurrentMonth: Boolean
        get() = !month.isBefore(YearMonth.from(today))

    fun recordsOf(person: Person): List<DayRecord> {
        return records.filter { record -> record.personId == person.id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MonthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    /** Re-read when the screen resumes, so "today" and the disabled next button stay correct. */
    private val todayDate = MutableStateFlow(LocalDate.now())

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    // The month travels with its records so a month change is never drawn with the old month's records.
    private val recordsOfSelectedMonth: Flow<Pair<YearMonth, List<DayRecord>>> =
        selectedMonth.flatMapLatest { month ->
            repository.observeDayRecords(month.atDay(1), month.atEndOfMonth())
                .map { records -> month to records }
        }

    val uiState: StateFlow<MonthUiState> = combine(
        todayDate,
        repository.observePeople(includeInactive = true),
        recordsOfSelectedMonth,
    ) { today, allPeople, monthAndRecords ->
        val month = monthAndRecords.first
        val records = monthAndRecords.second
        val personIdsWithRecords = records.map { record -> record.personId }.toSet()
        MonthUiState(
            month = month,
            today = today,
            people = allPeople.filter { person -> person.isActive || person.id in personIdsWithRecords },
            records = records,
            isLoaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MonthUiState(
            month = selectedMonth.value,
            today = todayDate.value,
            people = emptyList(),
            records = emptyList(),
            isLoaded = false,
        ),
    )

    fun refreshToday() {
        todayDate.value = LocalDate.now()
    }

    fun showPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        val nextMonth = selectedMonth.value.plusMonths(1)
        if (nextMonth.isAfter(YearMonth.from(todayDate.value))) {
            return
        }
        selectedMonth.value = nextMonth
    }

    fun saveRecord(record: DayRecord) {
        viewModelScope.launch { repository.saveDayRecord(record) }
    }

    fun clearRecord(personId: Long, date: LocalDate) {
        viewModelScope.launch { repository.clearDayRecord(personId, date) }
    }
}
