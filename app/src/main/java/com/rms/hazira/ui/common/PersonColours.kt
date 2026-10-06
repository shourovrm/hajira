package com.rms.hazira.ui.common

import androidx.compose.ui.graphics.Color

/**
 * The fixed set of colours a person can have. `Person.colourIndex` points into this list.
 * All are dark enough for white text on top.
 */
val personColours: List<Color> = listOf(
    Color(0xFF0E5A4B),
    Color(0xFFA8552A),
    Color(0xFF1F7A8C),
    Color(0xFF1E3A8A),
    Color(0xFF7A3B69),
    Color(0xFF4A5568),
    Color(0xFF8A6D1B),
    Color(0xFF3F7D3A),
)

/** Wraps around, so an index stored by a build with a longer palette still gets a colour. */
fun personColour(colourIndex: Int): Color {
    return personColours[Math.floorMod(colourIndex, personColours.size)]
}
