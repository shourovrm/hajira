package com.rms.hazira.ui.people

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rms.hazira.HaziraApp
import com.rms.hazira.domain.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [person] is null when adding, or when the id no longer exists. [isLoaded] is false until the
 * first read finishes, so the form is not built from empty values and then replaced.
 */
data class PersonEditUiState(
    val person: Person? = null,
    val isLoaded: Boolean = false,
)

class PersonEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<HaziraApp>().repository

    private val mutableUiState = MutableStateFlow(PersonEditUiState())
    val uiState: StateFlow<PersonEditUiState> = mutableUiState.asStateFlow()

    /** Reads the person once. The form is edited from this snapshot, not from live database changes. */
    fun load(personId: Long?) {
        if (mutableUiState.value.isLoaded) {
            return
        }
        viewModelScope.launch {
            val person = if (personId == null) null else repository.observePerson(personId).first()
            mutableUiState.value = PersonEditUiState(person = person, isLoaded = true)
        }
    }

    fun save(person: Person, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.savePerson(person)
            onSaved()
        }
    }

    fun delete(personId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deletePerson(personId)
            onDeleted()
        }
    }
}
