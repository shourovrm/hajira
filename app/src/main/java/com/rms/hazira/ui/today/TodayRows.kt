package com.rms.hazira.ui.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.isDueOn
import com.rms.hazira.ui.common.PersonAvatar
import com.rms.hazira.ui.common.statusWord
import com.rms.hazira.ui.theme.HaziraColours
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val expectedTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = HaziraColours.MutedText,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/** A due person with no mark yet: the two buttons are the whole point of the row. */
@Composable
internal fun StillToMarkRow(person: Person, onNegative: () -> Unit, onPositive: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, HaziraColours.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonAvatar(person = person)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val subtitle = roleAndTime(person)
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HaziraColours.MutedText,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNegative,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                ) {
                    Text(text = statusWord(DayStatus.ABSENT, person.kind), color = HaziraColours.AbsentRed)
                }
                Button(onClick = onPositive, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text(text = positiveButtonLabel(person))
                }
            }
        }
    }
}

@Composable
internal fun MarkedRow(entry: MarkedEntry, onClick: () -> Unit) {
    val person = entry.person
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(person = person)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = person.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = outcomeWords(person, entry.record),
                style = MaterialTheme.typography.bodyMedium,
                color = outcomeColour(entry.record.status),
                fontWeight = FontWeight.SemiBold,
            )
            if (entry.record.note.isNotBlank()) {
                Text(
                    text = entry.record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = HaziraColours.MutedText,
                )
            }
        }
    }
}

/** A quiet line: tapping it is for the rare make-up lesson or extra delivery. */
@Composable
internal fun NotDueRow(person: Person, shownDate: LocalDate, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = person.name,
            style = MaterialTheme.typography.bodyLarge,
            color = HaziraColours.MutedText,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = nextDueText(person, shownDate),
            style = MaterialTheme.typography.bodyMedium,
            color = HaziraColours.MutedText,
        )
    }
}

private fun positiveButtonLabel(person: Person): String {
    if (person.kind == ServiceKind.DELIVERY) {
        // The trailing dots tell the user this opens the amount sheet instead of saving at once.
        return statusWord(DayStatus.CAME, person.kind) + "…"
    }
    return statusWord(DayStatus.CAME, person.kind)
}

private fun roleAndTime(person: Person): String {
    val parts = mutableListOf<String>()
    if (person.role.isNotBlank()) {
        parts.add(person.role)
    }
    val expectedTime = person.expectedTime
    if (expectedTime != null) {
        parts.add(expectedTimeFormatter.format(expectedTime))
    }
    return parts.joinToString(" · ")
}

private fun nextDueText(person: Person, shownDate: LocalDate): String {
    for (daysAhead in 1L..7L) {
        val candidateDate = shownDate.plusDays(daysAhead)
        if (isDueOn(person, candidateDate)) {
            return "Next: " + formatWeekdayName(candidateDate.dayOfWeek)
        }
    }
    return "No days set"
}
