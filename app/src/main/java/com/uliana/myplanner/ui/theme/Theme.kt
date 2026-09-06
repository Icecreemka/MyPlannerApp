package com.uliana.myplanner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MyPlannerColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = Parchment,
    primaryContainer = SageLight,
    onPrimaryContainer = InkGreen,
    secondary = Terracotta,
    onSecondary = Parchment,
    secondaryContainer = TerracottaLight,
    tertiary = GoldenHour,
    background = Parchment,
    onBackground = InkGreen,
    surface = Parchment,
    onSurface = InkGreen,
    surfaceVariant = ParchmentDark,
    onSurfaceVariant = MossDeep,
    error = ErrorRust
)

private val MyPlannerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyPlannerColorScheme,
        typography = MyPlannerTypography,
        shapes = MyPlannerShapes,
        content = content
    )
}
