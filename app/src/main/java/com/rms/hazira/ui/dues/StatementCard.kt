package com.rms.hazira.ui.dues

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.MonthAccount
import com.rms.hazira.domain.Payment
import com.rms.hazira.domain.RateKind
import com.rms.hazira.domain.describeCharge
import com.rms.hazira.domain.formatMonthTitle
import com.rms.hazira.domain.formatPaymentDate
import com.rms.hazira.domain.formatTaka
import com.rms.hazira.ui.theme.HaziraColours

/** The month's days, the charge, each payment taken off, and the amount left to pay. */
@Composable
fun StatementCard(
    account: MonthAccount,
    records: List<DayRecord>,
    payments: List<Payment>,
    onDeletePayment: (Payment) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HaziraColours.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = statementHeading(account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DayChips(records = records)
            StatementLine(label = chargeLabel(account), amountText = formatTaka(account.chargeTaka))
            for (payment in payments) {
                PaymentLine(payment = payment, onDelete = { onDeletePayment(payment) })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface, thickness = 1.5.dp, modifier = Modifier.padding(top = 6.dp))
            TotalLine(account = account)
        }
    }
}

private fun statementHeading(account: MonthAccount): String {
    val monthTitle = formatMonthTitle(account.month)
    if (account.person.role.isBlank()) {
        return monthTitle
    }
    return monthTitle + ", " + account.person.role
}

private fun chargeLabel(account: MonthAccount): String {
    if (account.person.rateKind != RateKind.MONTHLY) {
        return describeCharge(account)
    }
    if (account.chargeTaka == 0) {
        return "Monthly fee, no day recorded"
    }
    return "Monthly fee"
}

/** Came days in green and absent days in red; days off and unmarked days are left out. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayChips(records: List<DayRecord>) {
    val markedRecords = records
        .filter { record -> record.status == DayStatus.CAME || record.status == DayStatus.ABSENT }
        .sortedBy { record -> record.date }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
    ) {
        for (record in markedRecords) {
            DayChip(dayOfMonth = record.date.dayOfMonth, isAbsent = record.status == DayStatus.ABSENT)
        }
    }
}

@Composable
private fun DayChip(dayOfMonth: Int, isAbsent: Boolean) {
    val backgroundColour = if (isAbsent) HaziraColours.AbsentRedSoft else HaziraColours.CoverGreenSoft
    val textColour = if (isAbsent) HaziraColours.AbsentRed else HaziraColours.CoverGreen
    Box(
        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(5.dp)).background(backgroundColour),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, color = textColour, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatementLine(label: String, amountText: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = amountText, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PaymentLine(payment: Payment, onDelete: () -> Unit) {
    val label = paymentLabel(payment)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = "− " + formatTaka(payment.amountTaka), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Delete payment of ${formatTaka(payment.amountTaka)}")
        }
    }
}

private fun paymentLabel(payment: Payment): String {
    val paidText = "Paid " + formatPaymentDate(payment.paidOn)
    if (payment.note.isBlank()) {
        return paidText
    }
    return paidText + ", " + payment.note
}

@Composable
private fun TotalLine(account: MonthAccount) {
    val label = when {
        account.dueTaka > 0 -> "To pay"
        account.dueTaka == 0 -> "Nothing to pay"
        else -> "Paid more than the charge"
    }
    val amountTaka = if (account.dueTaka < 0) -account.dueTaka else account.dueTaka

    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(text = formatTaka(amountTaka), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
