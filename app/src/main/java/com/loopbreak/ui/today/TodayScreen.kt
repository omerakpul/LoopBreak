package com.loopbreak.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.domain.model.LoopType
import com.loopbreak.ui.SyncUiState
import com.loopbreak.ui.components.CardTitle
import com.loopbreak.ui.components.RadialHourChart
import com.loopbreak.ui.components.ShadProgress
import com.loopbreak.ui.components.heroBrush
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadBadge
import com.loopbreak.ui.components.ShadBadgeVariant
import com.loopbreak.ui.components.ShadButton
import com.loopbreak.ui.components.ShadButtonVariant
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ShadIconButton
import com.loopbreak.ui.components.ShadSeparator
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.theme.Shad
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/** The event list is capped so one card never grows into hundreds of rows. */
private const val MAX_EVENT_ROWS = 50

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
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    color = Shad.colors.destructive,
                )
            }
        }

        item {
            ShadCard(brush = heroBrush()) {
                Text(
                    stringResource(R.string.today_hourly),
                    style = MaterialTheme.typography.labelLarge,
                    color = Shad.colors.mutedForeground,
                )
                Spacer(Modifier.height(8.dp))
                RadialHourChart(state.hourly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.score.toString(),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            stringResource(R.string.today_score_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Shad.colors.mutedForeground,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                val peak = state.hourly.indices.maxByOrNull { state.hourly[it] }
                    ?.takeIf { state.hourly[it] > 0 }
                MutedText(
                    if (peak != null) {
                        stringResource(R.string.today_peak, "%02d:00-%02d:00".format(peak, (peak + 1) % 24))
                    } else {
                        stringResource(R.string.today_score_caption)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (state.loaded && state.loops.isEmpty()) {
            item {
                ShadCard {
                    MutedText(
                        stringResource(R.string.today_empty),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (state.topApps.isNotEmpty()) {
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.today_top_apps))
                    val top = state.topApps.maxOf { it.total }.coerceAtLeast(1)
                    state.topApps.forEach { app ->
                        Spacer(Modifier.height(12.dp))
                        ShadSeparator()
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.titleSmall)
                                MutedText(
                                    stringResource(R.string.top_app_detail, app.returns, app.peeks, app.pingPongs),
                                )
                            }
                            ShadBadge(app.total.toString(), variant = ShadBadgeVariant.Default)
                        }
                        Spacer(Modifier.height(8.dp))
                        ShadProgress(app.total.toFloat() / top)
                    }
                }
            }
        }

        if (state.loops.isNotEmpty()) {
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.today_events))
                    state.loops.take(MAX_EVENT_ROWS).forEach { row ->
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
                        Spacer(Modifier.height(12.dp))
                        ShadSeparator()
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(row.title, style = MaterialTheme.typography.titleSmall)
                                MutedText(detail)
                            }
                            ShadBadge(
                                kind,
                                variant = ShadBadgeVariant.Outline,
                                dot = when (e.type) {
                                    LoopType.RE_OPEN -> Shad.colors.primary
                                    LoopType.QUICK_PEEK -> Shad.colors.warning
                                    LoopType.PING_PONG -> Shad.colors.violet
                                },
                            )
                        }
                    }
                    if (state.loops.size > MAX_EVENT_ROWS) {
                        Spacer(Modifier.height(12.dp))
                        MutedText(stringResource(R.string.more_items, state.loops.size - MAX_EVENT_ROWS))
                    }
                }
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
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShadIconButton("‹", onClick = onPrevious)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        ShadIconButton("›", onClick = onNext, enabled = canGoForward)
        Spacer(Modifier.weight(1f))
        ShadButton(
            text = stringResource(if (syncing) R.string.syncing else R.string.action_refresh),
            onClick = onRefresh,
            variant = ShadButtonVariant.Outline,
            enabled = !syncing,
        )
    }
}
