package com.familypulse.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FamilyPulseColors = lightColorScheme()

@Composable
fun FamilyPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FamilyPulseColors,
        content = content
    )
}
