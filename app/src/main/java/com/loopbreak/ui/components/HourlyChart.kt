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
import androidx.compose.ui.unit.dp

/** One bar per value, hand-drawn on a Canvas (see docs/ARCHITECTURE.md). */
@Composable
fun BarChart(
    values: List<Int>,
    axisLabels: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val bar = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier.describe(contentDescription)) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
            val slot = size.width / values.size.coerceAtLeast(1)
            val barWidth = slot * 0.65f
            val radius = CornerRadius(barWidth / 4f)
            val base = 4.dp.toPx()
            values.forEachIndexed { index, value ->
                val x = index * slot + (slot - barWidth) / 2f
                drawRoundRect(track, Offset(x, size.height - base), Size(barWidth, base), radius)
                if (value > 0) {
                    val h = (size.height - base) * value / max + base
                    drawRoundRect(bar, Offset(x, size.height - h), Size(barWidth, h), radius)
                }
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
