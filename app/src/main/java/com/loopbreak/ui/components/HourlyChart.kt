package com.loopbreak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
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
    /** One text per bar. When set, tapping a bar selects it and shows its text above the chart. */
    readouts: List<String>? = null,
) {
    val bar = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val over = Shad.colors.warning
    val goalLine = Shad.colors.mutedForeground
    val grow = rememberEntrance(values to goal)
    var selected by remember(values) { mutableStateOf<Int?>(null) }

    Column(modifier.describe(contentDescription)) {
        if (readouts != null) {
            Box(Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.CenterStart) {
                selected?.let {
                    Text(
                        readouts.getOrElse(it) { "" },
                        style = MaterialTheme.typography.labelLarge,
                        color = Shad.colors.foreground,
                    )
                }
            }
        }
        val tapToSelect = if (readouts == null) {
            Modifier
        } else {
            Modifier.pointerInput(values.size) {
                detectTapGestures { tap ->
                    val slotWidth = size.width / values.size.coerceAtLeast(1)
                    val index = (tap.x / slotWidth).toInt().coerceIn(0, values.lastIndex)
                    selected = if (selected == index) null else index
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().height(96.dp).then(tapToSelect)) {
            val max = maxOf(values.maxOrNull() ?: 0, goal, 1)
            val slot = size.width / values.size.coerceAtLeast(1)
            val barWidth = slot * 0.65f
            val radius = CornerRadius(barWidth / 2f)
            val base = 4.dp.toPx()
            values.forEachIndexed { index, value ->
                val x = index * slot + (slot - barWidth) / 2f
                drawRoundRect(track, Offset(x, size.height - base), Size(barWidth, base), radius)
                if (value > 0) {
                    val h = (size.height - base) * value / max * grow.value + base
                    // With a selection, the other bars step back.
                    val color = if (goal in 1 until value) over else bar
                    val faded = selected != null && selected != index
                    drawRoundRect(if (faded) color.copy(alpha = 0.35f) else color, Offset(x, size.height - h), Size(barWidth, h), radius)
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
    val readouts = values.mapIndexed { hour, value -> "%02d:00-%02d:00 · %d".format(hour, (hour + 1) % 24, value) }
    BarChart(values, listOf("00", "06", "12", "18", "24"), modifier, readouts = readouts)
}
