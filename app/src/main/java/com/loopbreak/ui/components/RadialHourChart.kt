package com.loopbreak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val REFERENCE_SIZE = 280.dp

/**
 * The day as a clock face: 24 bars radiate from a ring, midnight at the top and clockwise
 * from there. Busy hours grow longer and shift from the brand colour towards amber.
 * [center] is drawn in the middle of the ring (the score).
 */
@Composable
fun RadialHourChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    center: @Composable () -> Unit,
) {
    val c = Shad.colors
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = c.mutedForeground)

    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side: Dp = min(maxWidth.value, REFERENCE_SIZE.value).dp
        Box(Modifier.size(side), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(side)) {
                val k = size.width / REFERENCE_SIZE.toPx()
                val mid = Offset(size.width / 2f, size.height / 2f)
                val innerRadius = 80.dp.toPx() * k
                val longest = 38.dp.toPx() * k
                val shortest = 6.dp.toPx() * k
                val stroke = 7.dp.toPx() * k
                val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)

                fun direction(hour: Int): Offset {
                    val angle = Math.toRadians(-90.0 + hour * 15.0 + 7.5)
                    return Offset(cos(angle).toFloat(), sin(angle).toFloat())
                }

                values.forEachIndexed { hour, value ->
                    val dir = direction(hour)
                    val ratio = value.toFloat() / max
                    val length = if (value > 0) shortest + (longest - shortest) * ratio else shortest
                    val color = if (value > 0) lerp(c.primary, c.warning, ratio) else c.border
                    drawLine(
                        color = color,
                        start = mid + dir * innerRadius,
                        end = mid + dir * (innerRadius + length),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }

                listOf(0, 6, 12, 18).forEach { hour ->
                    val label = measurer.measure("%02d".format(hour), labelStyle)
                    val at = mid + direction(hour) * (innerRadius + longest + 13.dp.toPx() * k)
                    drawText(label, topLeft = at - Offset(label.size.width / 2f, label.size.height / 2f))
                }
            }
            center()
        }
    }
}
