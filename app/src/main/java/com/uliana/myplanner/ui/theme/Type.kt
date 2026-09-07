package com.uliana.myplanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val HeadingFamily = FontFamily.Serif
private val BodyFamily = FontFamily.SansSerif

val MyPlannerTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = HeadingFamily, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadingFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp
    )
)
