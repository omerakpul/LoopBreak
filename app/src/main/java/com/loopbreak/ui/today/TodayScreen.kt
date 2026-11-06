package com.loopbreak.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.loopbreak.domain.model.LoopType
import com.loopbreak.ui.SyncUiState
import com.loopbreak.ui.components.HourlyChart
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun formatTime(ms: Long): String =
    timeFormat.format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()))

@Composable
fun TodayScreen(
    contentPadding: PaddingValues,
    sync: SyncUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            DayPicker(
                title = when (state.dayOffset) {
                    0 -> stringResource(R.string.day_today)
                    -1 -> stringResource(R.string.day_yesterday)
                    else -> dateFormat.format(state.day)
                },
                canGoForward = state.dayOffset < 0,
                onPrevious = viewModel::previousDay,
                onNext = viewModel::nextDay,
                syncing = sync.syncing,
                onRefresh = onRefresh,
            )
        }
        if (sync.failed) {
            item {
                Text(
                    stringResource(R.string.sync_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item { ScoreCard(state.score) }
        item { SectionTitle(stringResource(R.string.today_hourly)) }
        item { HourlyChart(state.hourly, Modifier.fillMaxWidth()) }

        if (state.loaded && state.loops.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.today_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        if (state.topApps.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.today_top_apps)) }
            items(state.topApps, key = { "app-" + it.packageName }) { app ->
                ListRow(
                    title = app.label,
                    detail = stringResource(R.string.top_app_detail, app.returns, app.peeks, app.pingPongs),
                )
            }
        }

        if (state.loops.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.today_events)) }
            items(state.loops, key = { "loop-" + it.event.startTime + it.event.type + it.event.packageName }) { row ->
                val e = row.event
                val kind = when (e.type) {
                    LoopType.RE_OPEN -> stringResource(R.string.loop_type_re_open)
                    LoopType.QUICK_PEEK -> stringResource(R.string.loop_type_quick_peek)
                    LoopType.PING_PONG -> stringResource(R.string.loop_type_ping_pong)
                }
                val detail = when (e.type) {
                    LoopType.RE_OPEN -> stringResource(
                        R.string.loop_detail_re_open, e.count, formatTime(e.startTime), formatTime(e.endTime),
                    )
                    LoopType.QUICK_PEEK -> stringResource(
                        R.string.loop_detail_quick_peek, formatTime(e.startTime), (e.endTime - e.startTime) / 1000,
                    )
                    LoopType.PING_PONG -> stringResource(
                        R.string.loop_detail_ping_pong, e.count, formatTime(e.startTime), formatTime(e.endTime),
                    )
                }
                ListRow(title = "${row.title} · $kind", detail = detail)
            }
        }
    }
}

@Composable
private fun DayPicker(
    title: String,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    syncing: Boolean,
    onRefresh: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPrevious) { Text("‹") }
        Text(title, style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onNext, enabled = canGoForward) { Text("›") }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onRefresh, enabled = !syncing) {
                Text(stringResource(if (syncing) R.string.syncing else R.string.action_refresh))
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.today_score_label), style = MaterialTheme.typography.labelLarge)
            Text(score.toString(), style = MaterialTheme.typography.displayLarge)
            Text(
                stringResource(R.string.today_score_caption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun ListRow(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
