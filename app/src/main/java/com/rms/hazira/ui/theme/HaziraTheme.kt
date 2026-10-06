package com.rms.hazira.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colours of the paper attendance register the app is modelled on. */
object HaziraColours {
    /** The register's cover. Primary actions and anything marked as came. */
    val CoverGreen = Color(0xFF0E5A4B)
    val CoverGreenSoft = Color(0xFFDDEBE6)

    /** The page. */
    val Paper = Color(0xFFFAFBF7)

    /** The printed lines of the page. Grid borders. */
    val RuleBlue = Color(0xFFC3D3EA)

    /** Ballpoint blue. Ticks and counts. */
    val InkBlue = Color(0xFF1E3A8A)

    /** Red ink. Absences only. */
    val AbsentRed = Color(0xFFC4313D)
    val AbsentRedSoft = Color(0xFFF7DADD)

    /** Money still owed, and the highlight for today. */
    val OwedYellow = Color(0xFFE2A52B)
    val OwedYellowSoft = Color(0xFFFBF0D6)
    val OwedText = Color(0xFF7A5200)

    val Text = Color(0xFF16211F)
    val MutedText = Color(0xFF5C6B68)
    val Line = Color(0xFFDFE5E1)
}

private val haziraColourScheme = lightColorScheme(
    primary = HaziraColours.CoverGreen,
    onPrimary = Color.White,
    primaryContainer = HaziraColours.CoverGreenSoft,
    onPrimaryContainer = HaziraColours.CoverGreen,
    secondary = HaziraColours.InkBlue,
    onSecondary = Color.White,
    secondaryContainer = HaziraColours.CoverGreenSoft,
    onSecondaryContainer = HaziraColours.CoverGreen,
    tertiary = HaziraColours.OwedYellow,
    tertiaryContainer = HaziraColours.OwedYellowSoft,
    onTertiaryContainer = HaziraColours.OwedText,
    error = HaziraColours.AbsentRed,
    errorContainer = HaziraColours.AbsentRedSoft,
    background = HaziraColours.Paper,
    onBackground = HaziraColours.Text,
    surface = HaziraColours.Paper,
    onSurface = HaziraColours.Text,
    surfaceVariant = HaziraColours.Line,
    onSurfaceVariant = HaziraColours.MutedText,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    outline = HaziraColours.MutedText,
    outlineVariant = HaziraColours.Line,
)

/** Light only for now: the register colours have no dark counterpart yet. */
@Composable
fun HaziraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = haziraColourScheme, content = content)
}
