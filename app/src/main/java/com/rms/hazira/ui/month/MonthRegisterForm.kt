package com.rms.hazira.ui.month

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.domain.formatQuantity
import com.rms.hazira.domain.isDueOn
import com.rms.hazira.ui.theme.HaziraColours
import java.time.LocalDate
import java.time.YearMonth

private val dayCellWidth = 44.dp
private val personRowHeight = 48.dp
private val headerRowHeight = 40.dp
private val nameColumnWidth = 104.dp
private val totalColumnWidth = 56.dp
private val ruleWidth = 1.dp

/**
 * The paper register: people down the side, days across, a total at the end. The name and
 * total columns stay put while the days scroll, so every row has the same fixed height
 * and the three columns line up.
 */
@Composable
internal fun MonthRegisterForm(uiState: MonthUiState, onEditDay: (Person, LocalDate) -> Unit) {
    val month = uiState.month
    val dayScrollState = rememberScrollState()
    var viewportWidthPixels by remember { mutableIntStateOf(0) }
    val dayCellWidthPixels = with(LocalDensity.current) { dayCellWidth.toPx() }

    // A past month has no "today", so it opens on its last day instead.
    val dayToShow = if (YearMonth.from(uiState.today) == month) uiState.today.dayOfMonth else month.lengthOfMonth()
    LaunchedEffect(month, viewportWidthPixels, dayScrollState.maxValue) {
        val dayCentre = (dayToShow - 0.5f) * dayCellWidthPixels
        val target = (dayCentre - viewportWidthPixels / 2f).toInt()
        dayScrollState.scrollTo(target.coerceIn(0, dayScrollState.maxValue))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        NameColumn(people = uiState.people)
        Column(
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { size -> viewportWidthPixels = size.width }
                .horizontalScroll(dayScrollState),
        ) {
            DayNumberRow(month = month, today = uiState.today)
            for (person in uiState.people) {
                DayCellsRow(person = person, uiState = uiState, onEditDay = onEditDay)
            }
        }
        TotalColumn(uiState = uiState)
    }
}

@Composable
private fun NameColumn(people: List<Person>) {
    Column(modifier = Modifier.width(nameColumnWidth)) {
        Box(modifier = Modifier.height(headerRowHeight).fillMaxWidth().ruled())
        for (person in people) {
            Column(
                modifier = Modifier
                    .height(personRowHeight)
                    .fillMaxWidth()
                    .ruled()
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = person.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = HaziraColours.MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TotalColumn(uiState: MonthUiState) {
    Column(modifier = Modifier.width(totalColumnWidth)) {
        Box(
            modifier = Modifier.height(headerRowHeight).fillMaxWidth().ruled(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = HaziraColours.MutedText)
        }
        for (person in uiState.people) {
            Box(
                modifier = Modifier.height(personRowHeight).fillMaxWidth().ruled(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = monthCountNumber(person, uiState.recordsOf(person)),
                    color = HaziraColours.InkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun DayNumberRow(month: YearMonth, today: LocalDate) {
    Row {
        for (dayOfMonth in 1..month.lengthOfMonth()) {
            val isToday = month.atDay(dayOfMonth) == today
            Box(
                modifier = Modifier
                    .width(dayCellWidth)
                    .height(headerRowHeight)
                    .background(if (isToday) HaziraColours.OwedYellowSoft else Color.Transparent)
                    .ruled(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = HaziraColours.MutedText,
                )
            }
        }
    }
}

@Composable
private fun DayCellsRow(
    person: Person,
    uiState: MonthUiState,
    onEditDay: (Person, LocalDate) -> Unit,
) {
    Row {
        for (dayOfMonth in 1..uiState.month.lengthOfMonth()) {
            val date = uiState.month.atDay(dayOfMonth)
            val isInFuture = date.isAfter(uiState.today)
            val tint = if (date == uiState.today) HaziraColours.OwedYellowSoft else Color.Transparent
            val tapModifier = if (isInFuture) Modifier else Modifier.clickable { onEditDay(person, date) }
            Box(
                modifier = Modifier
                    .width(dayCellWidth)
                    .height(personRowHeight)
                    .background(tint)
                    .ruled()
                    .then(tapModifier),
                contentAlignment = Alignment.Center,
            ) {
                RegisterCellMark(
                    person = person,
                    record = uiState.recordsByPersonDay[PersonDay(person.id, date)],
                    isDue = isDueOn(person, date),
                    isInFuture = isInFuture,
                )
            }
        }
    }
}

@Composable
private fun RegisterCellMark(person: Person, record: DayRecord?, isDue: Boolean, isInFuture: Boolean) {
    if (record != null) {
        RecordedMark(person = person, record = record)
        return
    }
    if (!isDue) {
        MarkText(text = "–", colour = HaziraColours.MutedText)
        return
    }
    if (!isInFuture) {
        DashedRing()
    }
}

@Composable
private fun RecordedMark(person: Person, record: DayRecord) {
    when (record.status) {
        DayStatus.CAME -> {
            if (person.kind == ServiceKind.DELIVERY) {
                MarkText(text = formatQuantity(record.quantity), colour = HaziraColours.InkBlue)
            } else {
                MarkText(text = "✓", colour = HaziraColours.InkBlue)
            }
        }
        DayStatus.ABSENT -> MarkText(text = "✗", colour = HaziraColours.AbsentRed)
        DayStatus.HOLIDAY -> MarkText(text = "off", colour = HaziraColours.MutedText, fontSize = 11.sp)
    }
}

@Composable
private fun MarkText(text: String, colour: Color, fontSize: TextUnit = 17.sp) {
    Text(text = text, color = colour, fontSize = fontSize, fontWeight = FontWeight.Bold)
}

/** "Due, not marked yet": an empty circle waiting for a tick. */
@Composable
private fun DashedRing() {
    val ringSize = 20.dp
    Canvas(modifier = Modifier.size(ringSize).padding(1.dp)) {
        val dashLengthPx = 4.dp.toPx()
        drawCircle(
            color = HaziraColours.MutedText,
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLengthPx, dashLengthPx)),
            ),
        )
    }
}

/** The register's printed lines: one under and one to the right of the cell. */
private fun Modifier.ruled(): Modifier = drawBehind {
    val ruleWidthPixels = ruleWidth.toPx()
    drawLine(
        color = HaziraColours.RuleBlue,
        start = Offset(0f, size.height - ruleWidthPixels / 2),
        end = Offset(size.width, size.height - ruleWidthPixels / 2),
        strokeWidth = ruleWidthPixels,
    )
    drawLine(
        color = HaziraColours.RuleBlue,
        start = Offset(size.width - ruleWidthPixels / 2, 0f),
        end = Offset(size.width - ruleWidthPixels / 2, size.height),
        strokeWidth = ruleWidthPixels,
    )
}
