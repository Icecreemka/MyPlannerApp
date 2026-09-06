package com.uliana.myplanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Небо, которое плавно меняет цвет в зависимости от времени суток: рассвет, день,
 * закат, ночь — вместо статичного фона. Изюминка главного экрана "План на день".
 */
private data class SkyKeyframe(val hourOfDay: Float, val top: Color, val mid: Color, val bottom: Color)

private val skyKeyframes = listOf(
    SkyKeyframe(0f, Color(0xFF0B1026), Color(0xFF161B42), Color(0xFF262C55)),
    SkyKeyframe(5f, Color(0xFF20264F), Color(0xFF5A5487), Color(0xFFB07A82)),
    SkyKeyframe(6.5f, Color(0xFF44669C), Color(0xFFEE9E5C), Color(0xFFFCD79E)),
    SkyKeyframe(9f, Color(0xFF6EC0E8), Color(0xFFBFE6F2), Color(0xFFFBF6EA)),
    SkyKeyframe(12f, Color(0xFF4A9FDE), Color(0xFF9BD8F4), Color(0xFFF3FAFF)),
    SkyKeyframe(16f, Color(0xFF5B99CF), Color(0xFFA9DAF2), Color(0xFFFCF1D8)),
    SkyKeyframe(18f, Color(0xFF3E5C82), Color(0xFFE99A69), Color(0xFFF6C87A)),
    SkyKeyframe(19.5f, Color(0xFF2A2A4C), Color(0xFFAD5A79), Color(0xFFEF8A5B)),
    SkyKeyframe(21f, Color(0xFF15173C), Color(0xFF2E2A62), Color(0xFF564A88)),
    SkyKeyframe(24f, Color(0xFF0B1026), Color(0xFF161B42), Color(0xFF262C55))
)

private fun skyColorsFor(hourFraction: Float): Triple<Color, Color, Color> {
    var lower = skyKeyframes.first()
    var upper = skyKeyframes.last()
    for (i in 0 until skyKeyframes.size - 1) {
        if (hourFraction >= skyKeyframes[i].hourOfDay && hourFraction <= skyKeyframes[i + 1].hourOfDay) {
            lower = skyKeyframes[i]
            upper = skyKeyframes[i + 1]
            break
        }
    }
    val span = (upper.hourOfDay - lower.hourOfDay).takeIf { it != 0f } ?: 1f
    val t = ((hourFraction - lower.hourOfDay) / span).coerceIn(0f, 1f)
    return Triple(
        lerp(lower.top, upper.top, t),
        lerp(lower.mid, upper.mid, t),
        lerp(lower.bottom, upper.bottom, t)
    )
}

private fun isNightHour(hourFraction: Float) = hourFraction < 5.5f || hourFraction > 20.5f

@Composable
fun SkyBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(60_000)
        }
    }
    val hourFraction = now.hour + now.minute / 60f
    val (top, mid, bottom) = remember(hourFraction.toInt()) { skyColorsFor(hourFraction) }
    val night = isNightHour(hourFraction)

    val starSeed = remember { Random(42) }
    val stars = remember {
        List(40) { Offset(starSeed.nextFloat(), starSeed.nextFloat()) }
    }

    Box(modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(top, mid, bottom)))

            if (night) {
                stars.forEach { star ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f + 0.3f * sin(star.x * 10f)),
                        radius = 1.2f + (star.y % 1.5f),
                        center = Offset(star.x * size.width, star.y * size.height * 0.6f)
                    )
                }
            }

            // Солнце/луна: дуга от горизонта (внизу) до высшей точки в полдень/полночь.
            // Держим её в нижней части неба, чтобы не пряталась за плашкой с датой сверху.
            val isSunVisible = hourFraction in 5.5f..20.5f
            val cycleStart = if (isSunVisible) 5.5f else 20.5f
            val cycleEnd = if (isSunVisible) 20.5f else 29.5f
            val cycleHour = if (isSunVisible) hourFraction else if (hourFraction < 5.5f) hourFraction + 24f else hourFraction
            val progress = ((cycleHour - cycleStart) / (cycleEnd - cycleStart)).coerceIn(0f, 1f)
            val horizonY = size.height * 0.88f
            val arcHeight = size.height * 0.55f
            val bodyX = size.width * progress
            val bodyY = horizonY - arcHeight * sin(progress * PI).toFloat()
            val bodyColor = if (isSunVisible) Color(0xFFFFE9B0) else Color(0xFFEDEFF7)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(bodyColor.copy(alpha = 0.55f), bodyColor.copy(alpha = 0f)),
                    center = Offset(bodyX, bodyY),
                    radius = 140f
                ),
                radius = 140f,
                center = Offset(bodyX, bodyY)
            )
            drawCircle(color = bodyColor, radius = 30f, center = Offset(bodyX, bodyY))
        }
        content()
    }
}
