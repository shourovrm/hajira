package com.rms.hazira.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.formatQuantityWithUnit
import com.rms.hazira.ui.common.statusWord
import com.rms.hazira.ui.theme.HaziraColours
import java.time.LocalDate

private const val QUANTITY_STEP = 0.5
private const val MINIMUM_QUANTITY = 0.5

/**
 * The sheet for setting or clearing one person's mark on one day. Both the Today tab and the
 * Month tab use it.
 *
 * @param existingRecord The saved mark, or null when the day is not marked yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayMarkSheet(
    person: Person,
    date: LocalDate,
    existingRecord: DayRecord?,
    onSave: (DayRecord) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        DayMarkSheetContent(
            person = person,
            date = date,
            existingRecord = existingRecord,
            onSave = onSave,
            onClear = onClear,
        )
    }
}

@Composable
private fun DayMarkSheetContent(
    person: Person,
    date: LocalDate,
    existingRecord: DayRecord?,
    onSave: (DayRecord) -> Unit,
    onClear: () -> Unit,
) {
    var status by remember { mutableStateOf(existingRecord?.status ?: DayStatus.CAME) }
    var quantity by remember {
        mutableDoubleStateOf(existingRecord?.quantity ?: person.defaultQuantity)
    }
    var note by remember { mutableStateOf(existingRecord?.note ?: "") }

    val isDelivery = person.kind == ServiceKind.DELIVERY
    val asksForQuantity = isDelivery && status == DayStatus.CAME

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(text = person.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = formatLongDate(date),
                style = MaterialTheme.typography.bodyMedium,
                color = HaziraColours.MutedText,
            )
        }

        if (asksForQuantity) {
            QuantityStepper(
                quantity = quantity,
                unitName = person.unitName,
                onQuantityChange = { newQuantity -> quantity = newQuantity },
            )
            QuickQuantityChips(
                quantity = quantity,
                usualQuantity = person.defaultQuantity,
                unitName = person.unitName,
                onQuantityChange = { newQuantity -> quantity = newQuantity },
            )
        }

        StatusChoice(
            kind = person.kind,
            selectedStatus = status,
            onStatusChange = { newStatus -> status = newStatus },
        )

        OutlinedTextField(
            value = note,
            onValueChange = { newNote -> note = newNote },
            label = { Text("Note (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val savedQuantity = if (asksForQuantity) quantity else 1.0
                onSave(DayRecord(person.id, date, status, savedQuantity, note.trim()))
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(text = saveButtonLabel(person, status, quantity))
        }

        if (existingRecord != null) {
            TextButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(text = "Clear mark", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Double,
    unitName: String,
    onQuantityChange: (Double) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = { onQuantityChange((quantity - QUANTITY_STEP).coerceAtLeast(MINIMUM_QUANTITY)) },
            enabled = quantity - QUANTITY_STEP >= MINIMUM_QUANTITY,
        ) {
            Text(text = "−", fontSize = 22.sp)
        }
        Text(
            text = formatQuantityWithUnit(quantity, unitName),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        FilledTonalIconButton(onClick = { onQuantityChange(quantity + QUANTITY_STEP) }) {
            Text(text = "+", fontSize = 22.sp)
        }
    }
}

/** Half, the usual amount and double: the three amounts a household most often needs. */
@Composable
private fun QuickQuantityChips(
    quantity: Double,
    usualQuantity: Double,
    unitName: String,
    onQuantityChange: (Double) -> Unit,
) {
    val halfQuantity = (usualQuantity / 2).coerceAtLeast(MINIMUM_QUANTITY)
    val doubleQuantity = usualQuantity * 2
    val quickQuantities = buildList {
        // When the usual amount is already the minimum, half would be a duplicate chip.
        if (halfQuantity != usualQuantity) {
            add(halfQuantity)
        }
        add(usualQuantity)
        add(doubleQuantity)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        for (quickQuantity in quickQuantities) {
            FilterChip(
                selected = quantity == quickQuantity,
                onClick = { onQuantityChange(quickQuantity) },
                label = { Text(text = formatQuantityWithUnit(quickQuantity, unitName)) },
            )
        }
    }
}

@Composable
private fun StatusChoice(
    kind: ServiceKind,
    selectedStatus: DayStatus,
    onStatusChange: (DayStatus) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (status in DayStatus.entries) {
            FilterChip(
                selected = status == selectedStatus,
                onClick = { onStatusChange(status) },
                label = { Text(text = statusWord(status, kind)) },
            )
        }
    }
}

/** The button names what will be stored: "Save 1 litre", "Save as came". */
private fun saveButtonLabel(person: Person, status: DayStatus, quantity: Double): String {
    val isDeliveryThatArrived = person.kind == ServiceKind.DELIVERY && status == DayStatus.CAME
    if (isDeliveryThatArrived) {
        return "Save " + formatQuantityWithUnit(quantity, person.unitName)
    }
    return "Save as " + statusWord(status, person.kind).lowercase()
}
