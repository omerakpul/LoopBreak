package com.loopbreak.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.domain.model.LoopType
import com.loopbreak.ui.components.BarChart
import com.loopbreak.ui.components.CardTitle
import com.loopbreak.ui.components.HourlyChart
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadBadge
import com.loopbreak.ui.components.ShadBadgeVariant
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ChevronDirection
import com.loopbreak.ui.components.ShadChevronButton
import com.loopbreak.ui.components.ShadSeparator
import com.loopbreak.ui.components.ShadTabs
import com.loopbreak.ui.components.describe
import com.loopbreak.ui.components.heroBrush
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.theme.Shad
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ranges = listOf(7, 30)
private val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")
private val stampFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun at(ms: Long) = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())

@Composable
fun AppDetailScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShadChevronButton(ChevronDirection.Left, stringResource(R.string.back), onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Text(
                    viewModel.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                )
            }
        }

        item {
            ShadTabs(
                titles = ranges.map { stringResource(R.string.history_range, it) },
                selected = ranges.indexOf(state.rangeDays).coerceAtLeast(0),
                onSelect = { viewModel.setRange(ranges[it]) },
            )
        }

        item {
            ShadCard(brush = heroBrush()) {
                Text(
                    stringResource(R.string.history_total),
                    style = MaterialTheme.typography.labelLarge,
                    color = Shad.colors.mutedForeground,
                )
                Text(
                    state.total.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                MutedText(stringResource(R.string.top_app_detail, state.returns, state.peeks, state.pingPongs))
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.history_trend))
                Spacer(Modifier.height(16.dp))
                BarChart(
                    values = state.days.map { it.score },
                    axisLabels = listOfNotNull(
                        state.days.firstOrNull()?.date?.format(dayFormat),
                        state.days.lastOrNull()?.date?.format(dayFormat),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = stringResource(R.string.history_trend_desc, state.rangeDays, state.total),
                )
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.today_hourly))
                Spacer(Modifier.height(16.dp))
                HourlyChart(state.hourly, Modifier.fillMaxWidth().describe(stringResource(R.string.today_hourly)))
            }
        }

        if (state.recent.isEmpty() && state.loaded) {
            item {
                ShadCard {
                    MutedText(
                        stringResource(R.string.detail_empty),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else if (state.recent.isNotEmpty()) {
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.detail_recent))
                    state.recent.forEach { row ->
                        val e = row.event
                        val kind = when (e.type) {
                            LoopType.RE_OPEN -> stringResource(R.string.loop_type_re_open)
                            LoopType.QUICK_PEEK -> stringResource(R.string.loop_type_quick_peek)
                            LoopType.PING_PONG -> stringResource(R.string.loop_type_ping_pong)
                        }
                        val detail = when (e.type) {
                            LoopType.RE_OPEN -> stringResource(
                                R.string.loop_detail_re_open, e.count,
                                timeFormat.format(at(e.startTime)), timeFormat.format(at(e.endTime)),
                            )
                            LoopType.QUICK_PEEK -> stringResource(
                                R.string.loop_detail_quick_peek,
                                timeFormat.format(at(e.startTime)), (e.endTime - e.startTime) / 1000,
                            )
                            LoopType.PING_PONG -> stringResource(
                                R.string.loop_detail_ping_pong, e.count,
                                timeFormat.format(at(e.startTime)), timeFormat.format(at(e.endTime)),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        ShadSeparator()
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                val stamp = stampFormat.format(at(e.startTime))
                                Text(
                                    if (row.partner != null) "$stamp  ↔ ${row.partner}" else stamp,
                                    style = MaterialTheme.typography.titleSmall,
                                )
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
                }
            }
        }
    }
}
