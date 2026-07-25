package com.example.neotokopos85.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.neotokopos85.R

private val NeoTokoColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    secondary = SoftOrange,
    background = GradientTop,
    surface = CardBackground,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun NeoTokoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeoTokoColorScheme,
        typography = Typography,
        content = content
    )
}
val Cinzel = FontFamily(
    Font(R.font.cinzeldecorative_bold, FontWeight.Normal),
)

val Inknutan = FontFamily(
    Font(R.font.inknutantiqua_medium, FontWeight.Normal)
)