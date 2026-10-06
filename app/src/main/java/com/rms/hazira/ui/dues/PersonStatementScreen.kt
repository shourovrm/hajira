package com.rms.hazira.ui.dues

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.MonthAccount
import com.rms.hazira.domain.Payment
import com.rms.hazira.domain.formatMonthTitle
import com.rms.hazira.domain.formatPaymentDate
import com.rms.hazira.domain.formatTaka
import com.rms.hazira.ui.theme.HaziraColours
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonStatementScreen(personId: Long, month: YearMonth, onBack: () -> Unit) {
    val viewModel: PersonStatementViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // The month is local so tapping an earlier month changes this screen without a new route.
    var shownMonth by remember { mutableStateOf(month) }
    LaunchedEffect(personId, shownMonth) { viewModel.show(personId, shownMonth) }

    val person = uiState.person
    val account = uiState.account
    val isShowingChosenMonth = uiState.month == shownMonth

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = person?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (person != null && person.phone.isNotBlank()) {
                        CallButton(phone = person.phone)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (person == null || account == null || !isShowingChosenMonth) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            StatementContent(
                uiState = uiState,
                account = account,
                onShowMonth = { chosenMonth -> shownMonth = chosenMonth },
                onAddPayment = { amountTaka, note ->
                    viewModel.addPayment(amountTaka, LocalDate.now(), note)
                },
                onDeletePayment = { payment -> viewModel.deletePayment(payment.id) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun CallButton(phone: String) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))
            context.startActivity(dialIntent)
        },
    ) {
        Icon(imageVector = Icons.Filled.Call, contentDescription = "Call")
    }
}

@Composable
private fun StatementContent(
    uiState: PersonStatementUiState,
    account: MonthAccount,
    onShowMonth: (YearMonth) -> Unit,
    onAddPayment: (amountTaka: Int, note: String) -> Unit,
    onDeletePayment: (Payment) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddPaymentOpen by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        StatementCard(
            account = account,
            records = uiState.records,
            payments = uiState.payments,
            onDeletePayment = { payment -> paymentToDelete = payment },
        )
        StatementActions(
            statementText = uiState.statementText,
            onAddPayment = { isAddPaymentOpen = true },
        )
        EarlierMonths(earlierMonths = uiState.earlierMonths, onShowMonth = onShowMonth)
    }

    if (isAddPaymentOpen) {
        AddPaymentDialog(
            suggestedAmountTaka = account.dueTaka,
            paidOn = LocalDate.now(),
            onConfirm = { amountTaka, note ->
                onAddPayment(amountTaka, note)
                isAddPaymentOpen = false
            },
            onDismiss = { isAddPaymentOpen = false },
        )
    }

    val pendingDeletion = paymentToDelete
    if (pendingDeletion != null) {
        DeletePaymentDialog(
            payment = pendingDeletion,
            onConfirm = {
                onDeletePayment(pendingDeletion)
                paymentToDelete = null
            },
            onDismiss = { paymentToDelete = null },
        )
    }
}

@Composable
private fun StatementActions(statementText: String, onAddPayment: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, statementText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Send statement"))
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(text = "Send statement")
        }
        Button(onClick = onAddPayment, modifier = Modifier.weight(1f)) {
            Text(text = "Add payment")
        }
    }
}

@Composable
private fun DeletePaymentDialog(payment: Payment, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete this payment?") },
        text = {
            Text(text = "${formatTaka(payment.amountTaka)} paid on ${formatPaymentDate(payment.paidOn)} will be removed and the amount to pay goes up.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Keep")
            }
        },
    )
}

@Composable
private fun EarlierMonths(earlierMonths: List<EarlierMonth>, onShowMonth: (YearMonth) -> Unit) {
    if (earlierMonths.isEmpty()) {
        return
    }
    Text(
        text = "Earlier months",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
    )
    for (earlierMonth in earlierMonths) {
        EarlierMonthRow(earlierMonth = earlierMonth, onClick = { onShowMonth(earlierMonth.account.month) })
        HorizontalDivider()
    }
}

@Composable
private fun EarlierMonthRow(earlierMonth: EarlierMonth, onClick: () -> Unit) {
    val account = earlierMonth.account
    val isPaid = account.dueTaka <= 0

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = formatMonthTitle(account.month), style = MaterialTheme.typography.titleMedium)
            Text(
                text = describeDueReason(account),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatTaka(account.chargeTaka), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = earlierMonthStatus(account, earlierMonth.lastPaidOn),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPaid) HaziraColours.CoverGreen else HaziraColours.OwedText,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isPaid) HaziraColours.CoverGreenSoft else HaziraColours.OwedYellowSoft)
                    .padding(horizontal = 8.dp, vertical = 1.dp),
            )
        }
    }
}

private fun earlierMonthStatus(account: MonthAccount, lastPaidOn: LocalDate?): String {
    if (account.dueTaka > 0) {
        return "Due " + formatTaka(account.dueTaka)
    }
    if (lastPaidOn == null) {
        return "Paid"
    }
    return "Paid " + formatPaymentDate(lastPaidOn)
}
