package com.rms.hazira.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rms.hazira.domain.Person

/** A coloured circle with the first letter of the person's name. */
@Composable
fun PersonAvatar(person: Person, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val initial = person.name.trim().take(1).uppercase()
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(personColour(person.colourIndex)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
