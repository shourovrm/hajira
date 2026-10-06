package com.rms.hazira.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.ui.theme.HaziraColours

@Composable
fun TodayScreen(onAddPerson: () -> Unit, viewModel: TodayViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var personIdBeingMarked by remember { mutableStateOf<Long?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshToday()
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.isLoaded && uiState.people.isEmpty()) {
                EmptyState(onAddPerson = onAddPerson)
            } else {
                TodayList(
                    uiState = uiState,
                    onPreviousDay = viewModel::showPreviousDay,
                    onNextDay = viewModel::showNextDay,
                    onBackToToday = viewModel::showToday,
                    onQuickMark = { person, status ->
                        viewModel.markQuickly(person, uiState.shownDate, status)
                    },
                    onOpenMarkSheet = { person -> personIdBeingMarked = person.id },
                )
            }
        }
    }

    val personBeingMarked = uiState.people.find { person -> person.id == personIdBeingMarked }
    if (personBeingMarked != null) {
        val shownDate = uiState.shownDate
        DayMarkSheet(
            person = personBeingMarked,
            date = shownDate,
            existingRecord = uiState.records.find { record -> record.personId == personBeingMarked.id },
            onSave = { record ->
                viewModel.saveRecord(record)
                personIdBeingMarked = null
            },
            onClear = {
                viewModel.clearRecord(personBeingMarked.id, shownDate)
                personIdBeingMarked = null
            },
            onDismiss = { personIdBeingMarked = null },
        )
    }
}

@Composable
private fun TodayList(
    uiState: TodayUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onBackToToday: () -> Unit,
    onQuickMark: (Person, DayStatus) -> Unit,
    onOpenMarkSheet: (Person) -> Unit,
) {
    val stillToMark = uiState.stillToMark
    val marked = uiState.marked
    val notDue = uiState.notDue

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            TodayHeader(
                uiState = uiState,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onBackToToday = onBackToToday,
            )
        }
        item(key = "progress") {
            ProgressLine(markedCount = marked.size, stillToMarkCount = stillToMark.size)
        }

        if (stillToMark.isNotEmpty()) {
            item(key = "label-still-to-mark") { SectionLabel("Still to mark") }
            items(stillToMark, key = { person -> "still-${person.id}" }) { person ->
                StillToMarkRow(
                    person = person,
                    onNegative = { onQuickMark(person, DayStatus.ABSENT) },
                    onPositive = {
                        if (person.kind == ServiceKind.DELIVERY) {
                            onOpenMarkSheet(person)
                        } else {
                            onQuickMark(person, DayStatus.CAME)
                        }
                    },
                )
            }
        }

        if (marked.isNotEmpty()) {
            item(key = "label-marked") { SectionLabel("Marked") }
            items(marked, key = { entry -> "marked-${entry.person.id}" }) { entry ->
                MarkedRow(entry = entry, onClick = { onOpenMarkSheet(entry.person) })
            }
        }

        if (notDue.isNotEmpty()) {
            val label = if (uiState.isShowingToday) "Not expected today" else "Not expected on this day"
            item(key = "label-not-due") { SectionLabel(label) }
            items(notDue, key = { person -> "notdue-${person.id}" }) { person ->
                NotDueRow(
                    person = person,
                    shownDate = uiState.shownDate,
                    onClick = { onOpenMarkSheet(person) },
                )
            }
        }
    }
}

@Composable
private fun TodayHeader(
    uiState: TodayUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onBackToToday: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.isShowingToday) {
            Text(
                text = formatLongDate(uiState.shownDate),
                style = MaterialTheme.typography.bodyMedium,
                color = HaziraColours.MutedText,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = if (uiState.isShowingToday) "Today" else formatLongDate(uiState.shownDate)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous day",
                )
            }
            // No future dates: a day that has not happened cannot have been attended.
            IconButton(onClick = onNextDay, enabled = !uiState.isShowingToday) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next day",
                )
            }
        }
        if (!uiState.isShowingToday) {
            TextButton(onClick = onBackToToday, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(text = "Back to today")
            }
        }
    }
}

@Composable
private fun ProgressLine(markedCount: Int, stillToMarkCount: Int) {
    val totalCount = markedCount + stillToMarkCount
    if (totalCount == 0) {
        Text(
            text = "Nobody is due on this day.",
            style = MaterialTheme.typography.bodyMedium,
            color = HaziraColours.MutedText,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$markedCount of $totalCount marked", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = { markedCount.toFloat() / totalCount },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyState(onAddPerson: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Hazira records who came each day, what was delivered, and what you owe at the end of the month.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onAddPerson,
            modifier = Modifier.padding(top = 24.dp).heightIn(min = 48.dp),
        ) {
            Text(text = "Add a person")
        }
    }
}
