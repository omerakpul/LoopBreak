package com.loopbreak.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.domain.model.UsageEventType
import com.loopbreak.ui.components.CardDescription
import com.loopbreak.ui.components.ShadBadge
import com.loopbreak.ui.components.ShadBadgeVariant
import com.loopbreak.ui.components.ShadButton
import com.loopbreak.ui.components.ShadButtonVariant
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ShadIconButton
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.theme.Shad
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss")

@Composable
fun DebugScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DebugViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShadIconButton("‹", onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.debug_title), style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            ShadCard {
                Text(
                    stringResource(R.string.debug_count, state.totalCount),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(12.dp))
                ShadButton(
                    text = stringResource(R.string.debug_reset),
                    onClick = onReset,
                    variant = ShadButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item { CardDescription(stringResource(R.string.debug_recent)) }
        items(state.recent) { e ->
            val kind = when (e.type) {
                UsageEventType.RESUMED -> stringResource(R.string.debug_resumed)
                UsageEventType.PAUSED -> stringResource(R.string.debug_paused)
                null -> "?"
            }
            val time = timeFormat.format(Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault()))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Shad.colors.mutedForeground,
                    modifier = Modifier.width(112.dp),
                )
                ShadBadge(
                    kind,
                    variant = if (e.type == UsageEventType.RESUMED) ShadBadgeVariant.Default else ShadBadgeVariant.Outline,
                )
                Text(
                    e.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
