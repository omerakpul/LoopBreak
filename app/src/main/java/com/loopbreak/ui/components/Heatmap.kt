package com.loopbreak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Weekday x hour grid. [cells] has one list of 24 values per row; [rowLabels] names the rows.
 * Colour intensity is relative to the busiest cell.
 */
@Composable
fun Heatmap(cells: List<List<Int>>, rowLabels: List<String>, modifier: Modifier = Modifier) {
    val hot = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val cellHeight = 16.dp

    Column(modifier) {
        Row {
            Column(Modifier.width(32.dp)) {
                rowLabels.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(cellHeight),
                    )
                }
            }
            Canvas(Modifier.fillMaxWidth().height(cellHeight * cells.size)) {
                val max = (cells.maxOfOrNull { row -> row.maxOrNull() ?: 0 } ?: 0).coerceAtLeast(1)
                val cw = size.width / 24f
                val ch = size.height / cells.size.coerceAtLeast(1)
                val gap = 1.5.dp.toPx()
                val radius = CornerRadius(2.dp.toPx())
                cells.forEachIndexed { r, row ->
                    row.forEachIndexed { h, v ->
                        val color: Color = if (v <= 0) empty else hot.copy(alpha = 0.25f + 0.75f * v / max)
                        drawRoundRect(
                            color,
                            Offset(h * cw + gap / 2, r * ch + gap / 2),
                            Size(cw - gap, ch - gap),
                            radius,
                        )
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("00", "06", "12", "18", "24").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
