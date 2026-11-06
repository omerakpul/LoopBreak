package com.loopbreak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad

/* shadcn/ui building blocks (ui.shadcn.com/docs/components) for Compose. */

/** Card: 1dp border, rounded-xl, no elevation. */
@Composable
fun ShadCard(
    modifier: Modifier = Modifier,
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
        Column(Modifier.padding(16.dp), content = content)
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

/** Square outline button for single glyphs such as ‹ › − +. */
@Composable
fun ShadIconButton(
    glyph: String,
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
        Box(contentAlignment = Alignment.Center) {
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
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
        )
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
