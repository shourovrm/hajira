package com.rms.hazira.ui.month

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.isDueOn
import com.rms.hazira.ui.common.personColour
import com.rms.hazira.ui.theme.HaziraColours
import com.rms.hazira.ui.today.formatLongDate
import com.rms.hazira.ui.today.outcomeColour
import com.rms.hazira.ui.today.outcomeWords
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val weekdayLetters = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
private const val DAYS_PER_WEEK = 7
private const val DOTS_PER_ROW = 4
private val dotDiameter = 7.dp
private val dotGap = 3.dp
private val dotRingWidth = 1.5.dp

@Composable
internal fun MonthCalendarForm(uiState: MonthUiState, onEditDay: (Person, LocalDate) -> Unit) {
    val today = uiState.today
    var selectedDate by remember(uiState.month) {
        mutableStateOf<LocalDate?>(if (YearMonth.from(today) == uiState.month) today else null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PersonLegend(people = uiState.people)
        MonthGrid(
            uiState = uiState,
            selectedDate = selectedDate,
            onSelectDate = { date -> selectedDate = date },
        )
        DayPanel(uiState = uiState, date = selectedDate, onEditDay = onEditDay)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonLegend(people: List<Person>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (person in people) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(personColour(person.colourIndex)),
                )
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = HaziraColours.MutedText,
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    uiState: MonthUiState,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
) {
    val month = uiState.month
    // The week starts on Saturday in Bangladesh, so Saturday is column 0.
    val leadingBlankCount = Math.floorMod(
        month.atDay(1).dayOfWeek.value - DayOfWeek.SATURDAY.value,
        DAYS_PER_WEEK,
    )
    val weekCount = (leadingBlankCount + month.lengthOfMonth() + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (letters in weekdayLetters) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall,
                    color = HaziraColours.MutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        for (weekIndex in 0 until weekCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (columnIndex in 0 until DAYS_PER_WEEK) {
                    val dayOfMonth = weekIndex * DAYS_PER_WEEK + columnIndex - leadingBlankCount + 1
                    if (dayOfMonth in 1..month.lengthOfMonth()) {
                        val date = month.atDay(dayOfMonth)
                        CalendarDayCell(
                            date = date,
                            uiState = uiState,
                            isSelected = date == selectedDate,
                            onClick = { onSelectDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    uiState: MonthUiState,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val cellShape = RoundedCornerShape(8.dp)
    val backgroundColour = if (date == uiState.today) HaziraColours.OwedYellowSoft else Color.Transparent
    val borderColour = if (isSelected) HaziraColours.CoverGreen else Color.Transparent
    val personColours = uiState.people.map { person -> personColour(person.colourIndex) }
    val statuses = uiState.people.map { person ->
        uiState.recordsByPersonDay[PersonDay(person.id, date)]?.status
    }
    val dotRowCount = (uiState.people.size + DOTS_PER_ROW - 1) / DOTS_PER_ROW

    Column(
        modifier = modifier
            .padding(1.dp)
            .heightIn(min = 52.dp)
            .clip(cellShape)
            .background(backgroundColour)
            .border(1.5.dp, borderColour, cellShape)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = date.dayOfMonth.toString(), fontSize = 13.sp)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp)
                .height((dotDiameter + dotGap) * dotRowCount),
        ) {
            drawPersonDots(personColours = personColours, statuses = statuses)
        }
    }
}

/** Each person has a fixed slot, so a person's dots line up from one day to the next. */
private fun DrawScope.drawPersonDots(personColours: List<Color>, statuses: List<DayStatus?>) {
    val diameterPx = dotDiameter.toPx()
    val gapPx = dotGap.toPx()
    val ringWidthPx = dotRingWidth.toPx()
    val dotsInFirstRow = minOf(personColours.size, DOTS_PER_ROW)
    val rowWidthPx = dotsInFirstRow * diameterPx + (dotsInFirstRow - 1) * gapPx
    val startX = (size.width - rowWidthPx) / 2

    for (personIndex in personColours.indices) {
        val rowIndex = personIndex / DOTS_PER_ROW
        val columnIndex = personIndex % DOTS_PER_ROW
        val center = Offset(
            x = startX + columnIndex * (diameterPx + gapPx) + diameterPx / 2,
            y = rowIndex * (diameterPx + gapPx) + diameterPx / 2,
        )
        when (statuses[personIndex]) {
            DayStatus.CAME -> drawCircle(personColours[personIndex], radius = diameterPx / 2, center = center)
            DayStatus.ABSENT -> drawRing(HaziraColours.AbsentRed, diameterPx, ringWidthPx, center)
            DayStatus.HOLIDAY -> drawRing(HaziraColours.MutedText, diameterPx, ringWidthPx, center)
            null -> Unit
        }
    }
}

private fun DrawScope.drawRing(colour: Color, diameterPx: Float, ringWidthPx: Float, center: Offset) {
    // Inset by half the stroke so the ring fits inside the same slot a filled dot uses.
    drawCircle(
        color = colour,
        radius = (diameterPx - ringWidthPx) / 2,
        center = center,
        style = Stroke(width = ringWidthPx),
    )
}

@Composable
private fun DayPanel(
    uiState: MonthUiState,
    date: LocalDate?,
    onEditDay: (Person, LocalDate) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, HaziraColours.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (date == null) {
                Text(
                    text = "Tap a day to see who was due.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaziraColours.MutedText,
                )
                return@Column
            }
            Text(
                text = formatLongDate(date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val peopleOnDay = uiState.people.filter { person ->
                uiState.recordsByPersonDay.containsKey(PersonDay(person.id, date)) || isDueOn(person, date)
            }
            if (peopleOnDay.isEmpty()) {
                Text(
                    text = "Nobody was due and nothing was marked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaziraColours.MutedText,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            for (person in peopleOnDay) {
                val record = uiState.recordsByPersonDay[PersonDay(person.id, date)]
                DayPanelRow(
                    person = person,
                    record = record,
                    isInFuture = date.isAfter(uiState.today),
                    onClick = { onEditDay(person, date) },
                )
            }
        }
    }
}

@Composable
private fun DayPanelRow(
    person: Person,
    record: DayRecord?,
    isInFuture: Boolean,
    onClick: () -> Unit,
) {
    val rowModifier = if (isInFuture) Modifier else Modifier.clickable(onClick = onClick)
    Row(
        modifier = rowModifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(personColour(person.colourIndex)),
        )
        Text(
            text = person.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
        if (record != null) {
            Text(
                text = outcomeWords(person, record),
                style = MaterialTheme.typography.bodyMedium,
                color = outcomeColour(record.status),
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            val unmarkedText = if (isInFuture) "Not yet" else "Not marked"
            Text(
                text = unmarkedText,
                style = MaterialTheme.typography.bodyMedium,
                color = HaziraColours.MutedText,
            )
        }
    }
}
