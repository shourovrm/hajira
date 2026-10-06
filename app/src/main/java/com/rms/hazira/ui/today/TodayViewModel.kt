package com.rms.hazira.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rms.hazira.HaziraApp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import java.time.LocalDate
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

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    /** Re-read on every resume, so the screen follows the calendar if the app stays open overnight. */
    private val todayDate = MutableStateFlow(LocalDate.now())

    /** Null means "follow today". */
    private val chosenDate = MutableStateFlow<LocalDate?>(null)

    private val shownDate: Flow<LocalDate> = combine(todayDate, chosenDate) { today, chosen ->
        chosen ?: today
    }

    // The date travels with its records so a date change can never be drawn with the old day's records.
    private val recordsOfShownDate: Flow<Pair<LocalDate, List<DayRecord>>> =
        shownDate.flatMapLatest { date ->
            repository.observeDayRecords(date, date).map { records -> date to records }
        }

    val uiState: StateFlow<TodayUiState> = combine(
        todayDate,
        repository.observePeople(),
        recordsOfShownDate,
    ) { today, people, dateAndRecords ->
        TodayUiState(
            today = today,
            shownDate = dateAndRecords.first,
            people = people,
            records = dateAndRecords.second,
            isLoaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(
            today = todayDate.value,
            shownDate = todayDate.value,
            people = emptyList(),
            records = emptyList(),
            isLoaded = false,
        ),
    )

    fun refreshToday() {
        todayDate.value = LocalDate.now()
    }

    fun showPreviousDay() {
        val currentDate = chosenDate.value ?: todayDate.value
        chosenDate.value = currentDate.minusDays(1)
    }

    fun showNextDay() {
        val today = todayDate.value
        val nextDate = (chosenDate.value ?: today).plusDays(1)
        if (nextDate.isAfter(today)) {
            return
        }
        chosenDate.value = if (nextDate == today) null else nextDate
    }

    fun showToday() {
        chosenDate.value = null
    }

    /** One-tap marking for visits and outings, where the quantity carries no meaning. */
    fun markQuickly(person: Person, date: LocalDate, status: DayStatus) {
        saveRecord(DayRecord(personId = person.id, date = date, status = status))
    }

    fun saveRecord(record: DayRecord) {
        viewModelScope.launch { repository.saveDayRecord(record) }
    }

    fun clearRecord(personId: Long, date: LocalDate) {
        viewModelScope.launch { repository.clearDayRecord(personId, date) }
    }
}
