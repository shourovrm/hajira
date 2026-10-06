package com.rms.hazira.ui.people

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.ui.common.personColours
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonEditScreen(personId: Long?, onDone: () -> Unit) {
    val viewModel: PersonEditViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(personId) { viewModel.load(personId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (personId == null) "Add person" else "Edit person") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (!uiState.isLoaded) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            // Keyed on the loaded person so the form is built once from the stored values.
            val form = remember(uiState.person) { PersonForm(uiState.person) }
            PersonEditForm(
                form = form,
                existingPerson = uiState.person,
                onSave = {
                    val person = form.validateAndBuild()
                    if (person != null) {
                        viewModel.save(person, onSaved = onDone)
                    }
                },
                onDelete = { existingId -> viewModel.delete(existingId, onDeleted = onDone) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PersonEditForm(
    form: PersonForm,
    existingPerson: Person?,
    onSave: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = { text -> form.name = text },
            label = { Text(text = "Name") },
            isError = form.nameError != null,
            supportingText = errorText(form.nameError),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.role,
            onValueChange = { text -> form.role = text },
            label = { Text(text = "Role, for example Quran teacher") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        KindChoice(form = form)
        WeekdayChoice(form = form)
        ExpectedTimeChoice(form = form)
        RateChoice(form = form)
        if (form.isDelivery) {
            DeliveryFields(form = form)
        }
        OutlinedTextField(
            value = form.phone,
            onValueChange = { text -> form.phone = text },
            label = { Text(text = "Phone (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ColourChoice(form = form)
        if (existingPerson != null) {
            ActiveSwitch(form = form)
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Save")
        }
        if (existingPerson != null) {
            DeleteAction(personName = existingPerson.name, onConfirmed = { onDelete(existingPerson.id) })
        }
    }
}

private fun errorText(message: String?): (@Composable () -> Unit)? {
    if (message == null) {
        return null
    }
    return { Text(text = message) }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun KindChoice(form: PersonForm) {
    val choices = listOf(
        ServiceKind.VISIT to "Comes to the house",
        ServiceKind.OUTING to "We go there",
        ServiceKind.DELIVERY to "Delivery",
    )
    Column {
        SectionLabel(text = "Kind")
        for ((kind, label) in choices) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = form.kind == kind, role = Role.RadioButton, onClick = { form.selectKind(kind) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = form.kind == kind, onClick = null, modifier = Modifier.padding(12.dp))
                Text(text = label)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdayChoice(form: PersonForm) {
    Column {
        SectionLabel(text = "Days")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (weekday in weekdaysStartingSaturday) {
                FilterChip(
                    selected = weekday in form.weekdays,
                    onClick = { form.toggleWeekday(weekday) },
                    label = { Text(text = shortName(weekday)) },
                )
            }
            FilterChip(
                selected = form.weekdays.size == 7,
                onClick = { form.toggleEveryDay() },
                label = { Text(text = "Every day") },
            )
        }
        val error = form.weekdaysError
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ExpectedTimeChoice(form: PersonForm) {
    var isPickerOpen by remember { mutableStateOf(false) }
    val time = form.expectedTime

    Column {
        SectionLabel(text = "Expected time")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { isPickerOpen = true }) {
                Text(text = if (time == null) "Set a time" else formatExpectedTime(time))
            }
            if (time != null) {
                TextButton(onClick = { form.expectedTime = null }) {
                    Text(text = "Clear time")
                }
            }
        }
    }

    if (isPickerOpen) {
        TimePickerDialog(
            initialTime = time ?: LocalTime.of(16, 0),
            onConfirm = { chosenTime ->
                form.expectedTime = chosenTime
                isPickerOpen = false
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialTime: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
private fun RateChoice(form: PersonForm) {
    val secondRateKind = if (form.isDelivery) RateKind.PER_UNIT else RateKind.PER_VISIT
    val secondLabel = if (form.isDelivery) "Per unit" else "Per visit"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Rate")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = form.rateKind == RateKind.MONTHLY,
                onClick = { form.rateKind = RateKind.MONTHLY },
                label = { Text(text = "Monthly") },
            )
            FilterChip(
                selected = form.rateKind == secondRateKind,
                onClick = { form.rateKind = secondRateKind },
                label = { Text(text = secondLabel) },
            )
        }
        OutlinedTextField(
            value = form.rateText,
            onValueChange = { text -> form.rateText = text },
            label = { Text(text = "Amount in taka") },
            prefix = { Text(text = "৳") },
            isError = form.rateError != null,
            supportingText = errorText(form.rateError),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DeliveryFields(form: PersonForm) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = form.unitName,
            onValueChange = { text -> form.unitName = text },
            label = { Text(text = "Unit") },
            isError = form.unitError != null,
            supportingText = errorText(form.unitError),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = form.quantityText,
            onValueChange = { text -> form.quantityText = text },
            label = { Text(text = "Usual quantity") },
            isError = form.quantityError != null,
            supportingText = errorText(form.quantityError),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColourChoice(form: PersonForm) {
    Column {
        SectionLabel(text = "Colour")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            personColours.forEachIndexed { index, colour ->
                ColourSwatch(
                    colour = colour,
                    isSelected = form.colourIndex == index,
                    onClick = { form.colourIndex = index },
                )
            }
        }
    }
}

@Composable
private fun ColourSwatch(colour: Color, isSelected: Boolean, onClick: () -> Unit) {
    val borderColour = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colour)
            .border(width = 3.dp, color = borderColour, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected", tint = Color.White)
        }
    }
}

@Composable
private fun ActiveSwitch(form: PersonForm) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Active", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Turn off when the service has stopped. The history is kept.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = form.isActive, onCheckedChange = { checked -> form.isActive = checked })
    }
}

@Composable
private fun DeleteAction(personName: String, onConfirmed: () -> Unit) {
    var isConfirmationOpen by remember { mutableStateOf(false) }

    TextButton(
        onClick = { isConfirmationOpen = true },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(text = "Delete", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
    }

    if (isConfirmationOpen) {
        AlertDialog(
            onDismissRequest = { isConfirmationOpen = false },
            title = { Text(text = "Delete $personName?") },
            text = { Text(text = "Their attendance history and payments are deleted too. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = onConfirmed) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isConfirmationOpen = false }) {
                    Text(text = "Keep")
                }
            },
        )
    }
}
