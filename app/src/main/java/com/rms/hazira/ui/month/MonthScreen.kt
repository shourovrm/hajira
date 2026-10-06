package com.rms.hazira.ui.month

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.Person
import com.rms.hazira.ui.theme.HaziraColours
import com.rms.hazira.ui.today.DayMarkSheet
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

private enum class MonthForm(val label: String) {
    CALENDAR("Calendar"),
    STRIPS("Strips"),
    REGISTER("Register"),
}

private data class EditTarget(val personId: Long, val date: LocalDate)

@Composable
fun MonthScreen(viewModel: MonthViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedForm by rememberSaveable { mutableStateOf(MonthForm.CALENDAR) }
    var editTarget by remember { mutableStateOf<EditTarget?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshToday()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Month",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            MonthSwitcher(
                month = uiState.month,
                canGoForward = !uiState.isCurrentMonth,
                onPreviousMonth = viewModel::showPreviousMonth,
                onNextMonth = viewModel::showNextMonth,
            )
            FormChoice(selectedForm = selectedForm, onFormChange = { form -> selectedForm = form })

            if (uiState.isLoaded && uiState.people.isEmpty()) {
                Text(
                    text = "Nobody to show for this month yet. Add a person from the People tab.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HaziraColours.MutedText,
                )
            } else if (uiState.isLoaded) {
                val onEditDay = { person: Person, date: LocalDate ->
                    editTarget = EditTarget(person.id, date)
                }
                when (selectedForm) {
                    MonthForm.CALENDAR -> MonthCalendarForm(uiState = uiState, onEditDay = onEditDay)
                    MonthForm.STRIPS -> MonthStripsForm(uiState = uiState)
                    MonthForm.REGISTER -> MonthRegisterForm(uiState = uiState, onEditDay = onEditDay)
                }
            }
        }
    }

    val target = editTarget
    val personBeingEdited = uiState.people.find { person -> person.id == target?.personId }
    if (target != null && personBeingEdited != null) {
        DayMarkSheet(
            person = personBeingEdited,
            date = target.date,
            existingRecord = uiState.recordsByPersonDay[PersonDay(target.personId, target.date)],
            onSave = { record ->
                viewModel.saveRecord(record)
                editTarget = null
            },
            onClear = {
                viewModel.clearRecord(target.personId, target.date)
                editTarget = null
            },
            onDismiss = { editTarget = null },
        )
    }
}

@Composable
private fun MonthSwitcher(
    month: YearMonth,
    canGoForward: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
            )
        }
        Text(
            text = monthTitleFormatter.format(month),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        // No future months: nothing can be marked in them yet.
        IconButton(onClick = onNextMonth, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
            )
        }
    }
}

@Composable
private fun FormChoice(selectedForm: MonthForm, onFormChange: (MonthForm) -> Unit) {
    val forms = MonthForm.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        forms.forEachIndexed { index, form ->
            SegmentedButton(
                selected = form == selectedForm,
                onClick = { onFormChange(form) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = forms.size),
                label = { Text(text = form.label) },
            )
        }
    }
}
