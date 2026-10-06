package com.rms.hazira.ui.people

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rms.hazira.HaziraApp
import com.rms.hazira.domain.Person
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** [isLoaded] stops the empty state from flashing before the first database read. */
data class PeopleUiState(
    val people: List<Person> = emptyList(),
    val isLoaded: Boolean = false,
)

class PeopleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    val uiState: StateFlow<PeopleUiState> = repository.observePeople(includeInactive = true)
        .map { people -> PeopleUiState(people = activeFirst(people), isLoaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeopleUiState())

    /** The sort is stable, so people stay ordered by name inside each group. */
    private fun activeFirst(people: List<Person>): List<Person> {
        return people.sortedBy { person -> !person.isActive }
    }
}
