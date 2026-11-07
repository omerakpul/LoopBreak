package com.loopbreak.ui.today

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.loopbreak.domain.detect.DayDelta
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
import com.loopbreak.ui.components.ChevronDirection
import com.loopbreak.ui.components.ShadChevronButton
import com.loopbreak.ui.components.ShadIconButton
import com.loopbreak.ui.components.ShadSeparator
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.screenSpacing
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

/** Pull down to refresh. The spinner only shows for a pull, not for the sync that runs on every resume. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    contentPadding: PaddingValues,
    sync: SyncUiState,
    onRefresh: () -> Unit,
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    var pulled by remember { mutableStateOf(false) }
    LaunchedEffect(sync.syncing) { if (!sync.syncing) pulled = false }
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = pulled,
        onRefresh = {
            pulled = true
            onRefresh()
        },
        modifier = modifier.fillMaxSize(),
        state = pullState,
        // The header above the list is opaque, so the spinner has to sit below it.
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = pulled,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = contentPadding.calculateTopPadding()),
            )
        },
    ) {
        TodayContent(contentPadding, sync, onRefresh, onOpenApp, Modifier.fillMaxSize(), viewModel)
    }
}

@Composable
private fun TodayContent(
    contentPadding: PaddingValues,
    sync: SyncUiState,
    onRefresh: () -> Unit,
    onOpenApp: (String) -> Unit,
    modifier: Modifier,
    viewModel: TodayViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showScoreInfo by remember { mutableStateOf(false) }
    // Placeholders only while the very first sync is still running; later syncs keep the content.
    val showSkeleton = !state.loaded || (!state.hasData && sync.syncing)

    // The score counts up from zero when the screen opens or the day changes.
    var scoreTarget by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.score) { scoreTarget = state.score }
    val shownScore by animateIntAsState(scoreTarget, tween(900, easing = FastOutSlowInEasing), label = "score")

    // The app can stay alive past midnight: re-read the date whenever it comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }

    if (showScoreInfo) ScoreInfoSheet(state = state, onDismiss = { showScoreInfo = false })

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(screenSpacing),
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

        if (showSkeleton) {
            todaySkeleton()
            return@LazyColumn
        }

        item {
            ShadCard(brush = heroBrush()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.today_hourly),
                        style = MaterialTheme.typography.labelLarge,
                        color = Shad.colors.mutedForeground,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                    ShadIconButton(
                        glyph = "?",
                        description = stringResource(R.string.score_info_title),
                        onClick = { showScoreInfo = true },
                    )
                }
                Spacer(Modifier.height(8.dp))
                val peak = state.hourly.indices.maxByOrNull { state.hourly[it] }
                    ?.takeIf { state.hourly[it] > 0 }
                val insight = if (peak != null) {
                    stringResource(R.string.today_peak, "%02d:00-%02d:00".format(peak, (peak + 1) % 24))
                } else {
                    stringResource(R.string.today_score_caption)
                }
                RadialHourChart(
                    state.hourly,
                    stringResource(R.string.today_chart_desc, state.score, insight),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            shownScore.toString(),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            stringResource(R.string.today_score_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Shad.colors.mutedForeground,
                        )
                    }
                }
                // The comparison chip sits under the ring so the card title keeps its room.
                state.delta?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { DeltaBadge(it) }
                }
                if (state.goal > 0) {
                    val over = state.score > state.goal
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.goal_progress, state.score, state.goal),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (over) Shad.colors.warning else Shad.colors.mutedForeground,
                    )
                    Spacer(Modifier.height(6.dp))
                    ShadProgress(
                        fraction = state.score.toFloat() / state.goal,
                        color = if (over) Shad.colors.warning else Shad.colors.primary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                MutedText(
                    insight,
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
                        Column(
                            Modifier.clickable(
                                onClickLabel = stringResource(R.string.open_app_details, app.label),
                            ) { onOpenApp(app.packageName) },
                        ) {
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

/** "↓ 30% vs. previous day": teal when it got better, amber when it got worse. */
@Composable
private fun DeltaBadge(delta: DayDelta) {
    val text = when {
        delta.delta == 0 -> stringResource(R.string.delta_same)
        delta.isBetter -> stringResource(R.string.delta_down, delta.percent ?: 0)
        delta.percent != null -> stringResource(R.string.delta_up_pct, delta.percent)
        else -> stringResource(R.string.delta_up_abs, delta.delta)
    }
    ShadBadge(
        text = text,
        variant = ShadBadgeVariant.Outline,
        dot = when {
            delta.isBetter -> Shad.colors.primary
            delta.isWorse -> Shad.colors.warning
            else -> Shad.colors.mutedForeground
        },
    )
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
        ShadChevronButton(ChevronDirection.Left, stringResource(R.string.day_previous), onClick = onPrevious)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        ShadChevronButton(ChevronDirection.Right, stringResource(R.string.day_next), onClick = onNext, enabled = canGoForward)
        Spacer(Modifier.weight(1f))
        ShadButton(
            text = stringResource(if (syncing) R.string.syncing else R.string.action_refresh),
            onClick = onRefresh,
            variant = ShadButtonVariant.Outline,
            enabled = !syncing,
        )
    }
}
