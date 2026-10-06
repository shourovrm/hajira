package com.rms.hazira.ui.month

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rms.hazira.domain.DayRecord
import com.rms.hazira.domain.DayStatus
import com.rms.hazira.domain.Person
import com.rms.hazira.domain.ServiceKind
import com.rms.hazira.ui.common.personColour
import com.rms.hazira.ui.theme.HaziraColours
import java.time.YearMonth

private val stripHeight = 34.dp
private val axisDayNumbers = setOf(1, 5, 10, 15, 20, 25, 30)
private const val CAME_BAR_FRACTION = 0.6f
private const val ABSENT_BAR_FRACTION = 0.3f
private const val BAR_WIDTH_FRACTION = 0.7f

@Composable
internal fun MonthStripsForm(uiState: MonthUiState) {
    val dayCount = uiState.month.lengthOfMonth()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DayNumberAxis(dayCount = dayCount)
        for (person in uiState.people) {
            val personRecords = uiState.recordsOf(person)
            PersonStrip(person = person, personRecords = personRecords, month = uiState.month)
        }
        Text(
            text = "Tall bar: came, taller when more than the usual amount was delivered. " +
                "Short red bar: absent. Thin grey line: no record or day off.",
            style = MaterialTheme.typography.bodySmall,
            color = HaziraColours.MutedText,
        )
    }
}

/** One slot per day with no gaps, so it lines up with the strips drawn under it. */
@Composable
private fun DayNumberAxis(dayCount: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfMonth in 1..dayCount) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (dayOfMonth in axisDayNumbers) {
                    // The number is wider than its slot, so it is allowed to spill over the neighbours.
                    Text(
                        text = dayOfMonth.toString(),
                        fontSize = 10.sp,
                        color = HaziraColours.MutedText,
                        softWrap = false,
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonStrip(person: Person, personRecords: List<DayRecord>, month: YearMonth) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = monthCountText(person, personRecords),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = personColour(person.colourIndex),
            )
        }
        val recordsByDayOfMonth = personRecords.associateBy { record -> record.date.dayOfMonth }
        val barColour = personColour(person.colourIndex)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(stripHeight),
        ) {
            drawStrip(person, recordsByDayOfMonth, month.lengthOfMonth(), barColour)
        }
    }
}

private fun DrawScope.drawStrip(
    person: Person,
    recordsByDayOfMonth: Map<Int, DayRecord>,
    dayCount: Int,
    barColour: Color,
) {
    val slotWidth = size.width / dayCount
    val barWidth = slotWidth * BAR_WIDTH_FRACTION
    val thinLineHeight = 2.dp.toPx()
    val greyLineColour = HaziraColours.MutedText.copy(alpha = 0.4f)

    for (dayOfMonth in 1..dayCount) {
        val record = recordsByDayOfMonth[dayOfMonth]
        val left = (dayOfMonth - 1) * slotWidth + (slotWidth - barWidth) / 2
        var barHeight = thinLineHeight
        var colour = greyLineColour

        if (record != null && record.status == DayStatus.CAME) {
            barHeight = size.height * cameBarFraction(person, record)
            colour = barColour
        }
        if (record != null && record.status == DayStatus.ABSENT) {
            barHeight = size.height * ABSENT_BAR_FRACTION
            colour = HaziraColours.AbsentRed
        }

        drawRect(
            color = colour,
            topLeft = Offset(left, size.height - barHeight),
            size = Size(barWidth, barHeight),
        )
    }
}

/** Taller when a delivery is above the usual amount, so a double-milk day stands out. */
private fun cameBarFraction(person: Person, record: DayRecord): Float {
    val isAboveUsual = person.kind == ServiceKind.DELIVERY && record.quantity > person.defaultQuantity
    if (isAboveUsual) {
        return 1f
    }
    return CAME_BAR_FRACTION
}
