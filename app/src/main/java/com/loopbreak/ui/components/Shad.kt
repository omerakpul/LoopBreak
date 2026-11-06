package com.loopbreak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad
import kotlin.math.cos
import kotlin.math.sin

/* shadcn/ui building blocks (ui.shadcn.com/docs/components) for Compose. */

/** A soft vertical wash of the brand colour, for hero cards. */
@Composable
fun heroBrush(): Brush {
    val c = Shad.colors
    return Brush.verticalGradient(listOf(c.primary.copy(alpha = 0.18f), c.card))
}

/** Card: 1dp border, rounded-xl, no elevation. Pass [brush] for a tinted hero card. */
@Composable
fun ShadCard(
    modifier: Modifier = Modifier,
    brush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Shad.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = c.card,
        contentColor = c.foreground,
        border = BorderStroke(1.dp, c.border),
    ) {
        val inner = if (brush != null) Modifier.background(brush) else Modifier
        Column(inner.padding(16.dp), content = content)
    }
}

@Composable
fun CardTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
fun CardDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Shad.colors.mutedForeground,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
fun MutedText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = Shad.colors.mutedForeground,
        modifier = modifier,
        textAlign = textAlign,
    )
}

@Composable
fun ShadSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = Shad.colors.border)
}

enum class ShadButtonVariant { Default, Outline, Ghost, Destructive }

@Composable
fun ShadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ShadButtonVariant = ShadButtonVariant.Default,
    enabled: Boolean = true,
) {
    val c = Shad.colors
    val (container, content, border) = when (variant) {
        ShadButtonVariant.Default -> Triple(c.primary, c.primaryForeground, null)
        ShadButtonVariant.Outline -> Triple(c.background, c.foreground, BorderStroke(1.dp, c.input))
        ShadButtonVariant.Ghost -> Triple(Color.Transparent, c.foreground, null)
        ShadButtonVariant.Destructive -> Triple(c.background, c.destructive, BorderStroke(1.dp, c.input))
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(8.dp),
        color = container,
        contentColor = content,
        border = border,
    ) {
        Box(
            Modifier.defaultMinSize(minHeight = 36.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Describes a visual-only element (chart) to screen readers. */
fun Modifier.describe(text: String?): Modifier =
    if (text == null) this else semantics { contentDescription = text }

/** Square outline button for single glyphs such as ‹ › − +. [description] is what screen readers say. */
@Composable
fun ShadIconButton(
    glyph: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = Shad.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(36.dp).alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(8.dp),
        color = c.background,
        contentColor = c.foreground,
        border = BorderStroke(1.dp, c.input),
    ) {
        Box(Modifier.clearAndSetSemantics { contentDescription = description }, contentAlignment = Alignment.Center) {
            Text(glyph, style = MaterialTheme.typography.titleMedium)
        }
    }
}

enum class ShadBadgeVariant { Default, Secondary, Outline }

@Composable
fun ShadBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: ShadBadgeVariant = ShadBadgeVariant.Secondary,
    dot: Color? = null,
) {
    val c = Shad.colors
    val (container, content, border) = when (variant) {
        ShadBadgeVariant.Default -> Triple(c.primary, c.primaryForeground, null)
        ShadBadgeVariant.Secondary -> Triple(c.secondary, c.foreground, null)
        ShadBadgeVariant.Outline -> Triple(Color.Transparent, c.foreground, BorderStroke(1.dp, c.border))
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
        border = border,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dot != null) {
                Box(Modifier.padding(end = 6.dp).size(7.dp).clip(RoundedCornerShape(50)).background(dot))
            }
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Thin progress bar: muted track, brand fill. */
@Composable
fun ShadProgress(fraction: Float, modifier: Modifier = Modifier, color: Color = Shad.colors.primary) {
    Box(
        modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(Shad.colors.muted),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

/** The app mark: a ring with a gap at the top and an arrowhead, i.e. a loop being broken. */
@Composable
fun LoopMark(size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier, color: Color = Shad.colors.primary) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val radius = s * 0.36f
        val stroke = s * 0.11f
        val mid = Offset(s / 2f, s / 2f)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(mid.x - radius, mid.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        // Arrowhead at the end of the arc (240 deg), pointing along the clockwise tangent.
        val end = Math.toRadians(240.0)
        val tip0 = Offset(mid.x + radius * cos(end).toFloat(), mid.y + radius * sin(end).toFloat())
        val tangent = Offset((-sin(end)).toFloat(), cos(end).toFloat())
        val normal = Offset(-tangent.y, tangent.x)
        val head = s * 0.2f
        val tip = tip0 + tangent * (head * 0.9f)
        val b1 = tip0 + normal * (head * 0.55f)
        val b2 = tip0 - normal * (head * 0.55f)
        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(b1.x, b1.y)
            lineTo(b2.x, b2.y)
            close()
        }
        drawPath(path, color)
        drawPath(path, color, style = Stroke(width = stroke * 0.3f, join = StrokeJoin.Round))
    }
}

/** Tabs: muted pill track with a raised active trigger. */
@Composable
fun ShadTabs(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Shad.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.muted)
            .padding(3.dp),
    ) {
        titles.forEachIndexed { index, title ->
            val active = index == selected
            Surface(
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f).height(30.dp),
                shape = RoundedCornerShape(7.dp),
                color = if (active) c.tabActive else Color.Transparent,
                contentColor = if (active) c.foreground else c.mutedForeground,
                border = if (active) BorderStroke(1.dp, c.border) else null,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(title, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
