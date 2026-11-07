package com.loopbreak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad

/** One bar per value, hand-drawn on a Canvas (see docs/ARCHITECTURE.md). */
@Composable
fun BarChart(
    values: List<Int>,
    axisLabels: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    /** Draws a dashed goal line and colours bars above it; 0 = no goal. */
    goal: Int = 0,
) {
    val bar = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val over = Shad.colors.warning
    val goalLine = Shad.colors.mutedForeground

    Column(modifier.describe(contentDescription)) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val max = maxOf(values.maxOrNull() ?: 0, goal, 1)
            val slot = size.width / values.size.coerceAtLeast(1)
            val barWidth = slot * 0.65f
            val radius = CornerRadius(barWidth / 4f)
            val base = 4.dp.toPx()
            values.forEachIndexed { index, value ->
                val x = index * slot + (slot - barWidth) / 2f
                drawRoundRect(track, Offset(x, size.height - base), Size(barWidth, base), radius)
                if (value > 0) {
                    val h = (size.height - base) * value / max + base
                    drawRoundRect(if (goal in 1 until value) over else bar, Offset(x, size.height - h), Size(barWidth, h), radius)
                }
            }
            if (goal > 0) {
                val y = size.height - base - (size.height - base) * goal / max
                drawLine(
                    color = goalLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            axisLabels.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 24 bars, one per hour of the day. */
@Composable
fun HourlyChart(values: List<Int>, modifier: Modifier = Modifier) {
    BarChart(values, listOf("00", "06", "12", "18", "24"), modifier)
}
