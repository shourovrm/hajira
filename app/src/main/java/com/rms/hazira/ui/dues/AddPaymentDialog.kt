package com.rms.hazira.ui.dues

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rms.hazira.domain.formatPaymentDate
import java.time.LocalDate

/**
 * Asks for a payment amount and an optional note. The payment is dated [paidOn], which the
 * caller sets to today.
 */
@Composable
fun AddPaymentDialog(
    suggestedAmountTaka: Int,
    paidOn: LocalDate,
    onConfirm: (amountTaka: Int, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf(if (suggestedAmountTaka > 0) suggestedAmountTaka.toString() else "") }
    var note by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { text -> amountText = text },
                    label = { Text(text = "Amount in taka") },
                    prefix = { Text(text = "৳") },
                    isError = amountError != null,
                    supportingText = amountError?.let { message -> { Text(text = message) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Paid on ${formatPaymentDate(paidOn)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { text -> note = text },
                    label = { Text(text = "Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountTaka = amountText.trim().toIntOrNull()
                    if (amountTaka == null || amountTaka <= 0) {
                        amountError = "Enter an amount above zero"
                    } else {
                        onConfirm(amountTaka, note.trim())
                    }
                },
            ) {
                Text(text = "Save payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}
