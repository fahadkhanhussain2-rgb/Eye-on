package com.familypulse.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF3157D5),
    onPrimary = Color.White,
    secondary = Color(0xFF536DCC),
    background = Color(0xFFF7F8FC),
    surface = Color.White,
    onBackground = Color(0xFF171923),
    onSurface = Color(0xFF171923)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9EAEFF),
    onPrimary = Color(0xFF10131C),
    secondary = Color(0xFFB8C2FF),
    background = Color(0xFF10131C),
    surface = Color(0xFF191D28),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun FamilyPulseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = if (darkTheme) {
            DarkColors
        } else {
            LightColors
        },
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

@Composable
fun FamilyPulseLogo(
    size: Dp
) {

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.primary
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "FP",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = (size.value * 0.34f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
