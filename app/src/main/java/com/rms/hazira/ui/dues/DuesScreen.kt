package com.rms.hazira.ui.dues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.MonthAccount
import com.rms.hazira.domain.formatMonthTitle
import com.rms.hazira.domain.formatTaka
import com.rms.hazira.ui.common.PersonAvatar
import com.rms.hazira.ui.theme.HaziraColours
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DuesScreen(onOpenStatement: (personId: Long, month: YearMonth) -> Unit) {
    val viewModel: DuesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MonthSwitcher(
                month = uiState.month,
                canShowNextMonth = uiState.canShowNextMonth,
                onPrevious = viewModel::showPreviousMonth,
                onNext = viewModel::showNextMonth,
            )
            Text(
                text = "Dues",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (uiState.isLoaded && uiState.accounts.isEmpty()) {
                EmptyMonth(month = uiState.month)
            } else {
                AccountList(uiState = uiState, onOpenStatement = onOpenStatement)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(
    month: YearMonth,
    canShowNextMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = formatMonthTitle(month),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = onNext, enabled = canShowNextMonth) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun EmptyMonth(month: YearMonth) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nothing is recorded for ${formatMonthTitle(month)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Mark who came on the Today tab and the amounts appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun AccountList(uiState: DuesUiState, onOpenStatement: (Long, YearMonth) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            DueBanner(uiState = uiState)
        }
        items(items = uiState.accounts, key = { account -> account.person.id }) { account ->
            AccountRow(account = account, onClick = { onOpenStatement(account.person.id, account.month) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun DueBanner(uiState: DuesUiState) {
    val monthName = uiState.month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val paidFraction = paidFraction(uiState)

    Surface(
        color = HaziraColours.CoverGreen,
        contentColor = androidx.compose.ui.graphics.Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(text = "Still to pay for $monthName", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatTaka(uiState.stillToPayTaka),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            PaidProgressBar(paidFraction = paidFraction)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Paid ${formatTaka(uiState.paidTaka)}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Month total ${formatTaka(uiState.monthTotalTaka)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun paidFraction(uiState: DuesUiState): Float {
    if (uiState.monthTotalTaka <= 0) {
        return 0f
    }
    return (uiState.paidTaka.toFloat() / uiState.monthTotalTaka).coerceIn(0f, 1f)
}

@Composable
private fun PaidProgressBar(paidFraction: Float) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(paidFraction)
                .height(6.dp)
                .background(HaziraColours.OwedYellow),
        )
    }
}

@Composable
private fun AccountRow(account: MonthAccount, onClick: () -> Unit) {
    val isPaid = account.dueTaka <= 0

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PersonAvatar(person = account.person)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = account.person.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = describeDueReason(account),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTaka(account.chargeTaka),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            PaidBadge(isPaid = isPaid)
        }
    }
}

@Composable
private fun PaidBadge(isPaid: Boolean) {
    val backgroundColour = if (isPaid) HaziraColours.CoverGreenSoft else HaziraColours.OwedYellowSoft
    val textColour = if (isPaid) HaziraColours.CoverGreen else HaziraColours.OwedText
    Text(
        text = if (isPaid) "Paid" else "Due",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = textColour,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(backgroundColour)
            .padding(horizontal = 8.dp, vertical = 1.dp),
    )
}
