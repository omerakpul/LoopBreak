package com.loopbreak.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.domain.model.UsageEventType
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
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    Text(stringResource(R.string.debug_title), style = MaterialTheme.typography.titleLarge)
                }
                Text(stringResource(R.string.debug_count, state.totalCount), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.debug_reset))
                }
                Text(stringResource(R.string.debug_recent), style = MaterialTheme.typography.labelLarge)
            }
        }
        items(state.recent) { e ->
            val kind = when (e.type) {
                UsageEventType.RESUMED -> stringResource(R.string.debug_resumed)
                UsageEventType.PAUSED -> stringResource(R.string.debug_paused)
                null -> "?"
            }
            val time = timeFormat.format(Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault()))
            Text("$time  $kind  ${e.label}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 1.dp))
        }
    }
}
