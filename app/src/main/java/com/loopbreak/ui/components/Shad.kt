package com.loopbreak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad

/* Building blocks of the app's design system. */

/** A soft vertical wash of the brand colour, for hero cards. */
@Composable
fun heroBrush(): Brush {
    val c = Shad.colors
    return Brush.verticalGradient(listOf(c.primary.copy(alpha = 0.07f), c.card))
}

/** Card: quiet border, generous corners, no elevation. Pass [brush] for a tinted hero card. */
@Composable
fun ShadCard(
    modifier: Modifier = Modifier,
    brush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Shad.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = c.card,
        contentColor = c.foreground,
        border = BorderStroke(1.dp, c.border),
    ) {
        // Dark theme only: a whisper of light from the top so cards feel lit rather than flat.
        // On a white card the same trick would just look dirty.
        val light = if (c.card.luminance() < 0.5f) {
            Brush.verticalGradient(listOf(lerp(c.card, c.foreground, 0.035f), c.card))
        } else {
            SolidColor(c.card)
        }
        Column(Modifier.background(brush ?: light).padding(22.dp), content = content)
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
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = Shad.colors.border.copy(alpha = 0.7f))
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
        ShadButtonVariant.Outline -> Triple(c.control, c.foreground, BorderStroke(1.dp, c.border))
        ShadButtonVariant.Ghost -> Triple(Color.Transparent, c.foreground, null)
        ShadButtonVariant.Destructive -> Triple(c.control, c.destructive, BorderStroke(1.dp, c.border))
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = content,
        border = border,
    ) {
        Box(
            Modifier.defaultMinSize(minHeight = 42.dp).padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Modal bottom sheet in the app's look: card background, small drag handle, rounded top corners. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Shad.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.card,
        contentColor = c.foreground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(c.input),
            )
        },
    ) {
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp).navigationBarsPadding(),
            content = content,
        )
    }
}

/** Describes a visual-only element (chart) to screen readers. */
fun Modifier.describe(text: String?): Modifier =
    if (text == null) this else semantics { contentDescription = text }

/** Shared look of the small square buttons: soft fill, quiet border, 12dp corners. */
@Composable
private fun IconButtonSurface(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = Shad.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(42.dp).alpha(if (enabled) 1f else 0.45f),
        shape = RoundedCornerShape(14.dp),
        color = c.control,
        contentColor = c.foreground,
        border = BorderStroke(1.dp, c.border),
    ) {
        Box(
            Modifier.clearAndSetSemantics { contentDescription = description },
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

/** Square button for a single glyph such as − + ?. [description] is what screen readers say. */
@Composable
fun ShadIconButton(
    glyph: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButtonSurface(description, onClick, modifier, enabled) {
        Text(glyph, style = MaterialTheme.typography.titleMedium)
    }
}

enum class ChevronDirection { Left, Right }

/** Square button with a drawn chevron: previous / next, back. */
@Composable
fun ShadChevronButton(
    direction: ChevronDirection,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButtonSurface(description, onClick, modifier, enabled) {
        val color = LocalContentColor.current
        Canvas(Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            val arm = if (direction == ChevronDirection.Left) w * 0.62f else w * 0.38f
            val tip = if (direction == ChevronDirection.Left) w * 0.36f else w * 0.64f
            val path = Path().apply {
                moveTo(arm, h * 0.22f)
                lineTo(tip, h * 0.5f)
                lineTo(arm, h * 0.78f)
            }
            drawPath(path, color, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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
        ShadBadgeVariant.Default -> Triple(c.primary.copy(alpha = 0.16f), c.primary, null)
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

/** Pulsing placeholder block for content that is still loading. */
@Composable
fun ShadSkeleton(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val pulse = rememberInfiniteTransition(label = "skeleton")
    val alpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeleton-alpha",
    )
    Box(modifier.clip(shape).background(Shad.colors.muted.copy(alpha = alpha)))
}

/** Thin progress bar: muted track, brand fill. */
@Composable
fun ShadProgress(fraction: Float, modifier: Modifier = Modifier, color: Color = Shad.colors.primary) {
    // Start empty and animate to the value, also when the value later changes.
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(fraction) { target = fraction }
    val shown by animateFloatAsState(target, tween(700, easing = FastOutSlowInEasing), label = "progress")
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
                .fillMaxWidth(shown.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

/**
 * The app mark: an infinity sign whose right loop is broken, with a dot in the break.
 * Drawn in a 100-unit box so it matches the launcher icon exactly.
 */
@Composable
fun LoopMark(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = Shad.colors.primary,
    accent: Color = Shad.colors.warning,
) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 100f
        val loop = Path().apply {
            moveTo(50f * k, 50f * k)
            cubicTo(58f * k, 32f * k, 88f * k, 32f * k, 88f * k, 50f * k)
            cubicTo(88f * k, 68f * k, 58f * k, 68f * k, 50f * k, 50f * k)
            cubicTo(42f * k, 32f * k, 12f * k, 32f * k, 12f * k, 50f * k)
            cubicTo(12f * k, 68f * k, 42f * k, 68f * k, 50f * k, 50f * k)
        }
        // The break sits between 8% and 18% of the way along the path (the top of the right loop).
        val measure = PathMeasure().apply { setPath(loop, false) }
        val length = measure.length
        val rest = Path().also { measure.getSegment(0.18f * length, length, it, true) }
        val lead = Path().also { measure.getSegment(0f, 0.08f * length, it, true) }
        val stroke = Stroke(width = 9f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(rest, color, style = stroke)
        drawPath(lead, color, style = stroke)
        drawCircle(accent, radius = 4.5f * k, center = Offset(72f * k, 36.5f * k))
    }
}

/** Tabs: a soft track with a raised indicator that slides to the selected trigger. */
@Composable
fun ShadTabs(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Shad.colors
    val triggerShape = RoundedCornerShape(14.dp)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.muted)
            .padding(4.dp),
    ) {
        val slot = maxWidth / titles.size
        val offset by animateDpAsState(
            targetValue = slot * selected.coerceAtLeast(0),
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "tab-offset",
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(slot)
                .height(34.dp)
                .clip(triggerShape)
                .background(c.tabActive),
        )
        Row {
            titles.forEachIndexed { index, title ->
                val active = index == selected
                val textColor by animateColorAsState(
                    targetValue = if (active) c.foreground else c.mutedForeground,
                    animationSpec = tween(200),
                    label = "tab-color",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(triggerShape)
                        .clickable(role = Role.Tab) { onSelect(index) }
                        .semantics { this.selected = active },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(title, style = MaterialTheme.typography.labelLarge, color = textColor, maxLines = 1)
                }
            }
        }
    }
}
