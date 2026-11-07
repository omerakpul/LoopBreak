package com.loopbreak.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.ui.components.BarChart
import com.loopbreak.ui.components.CardDescription
import com.loopbreak.ui.components.CardTitle
import com.loopbreak.ui.components.Heatmap
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadBadge
import com.loopbreak.ui.components.ShadBadgeVariant
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ShadSeparator
import com.loopbreak.ui.components.ShadTabs
import com.loopbreak.ui.components.heroBrush
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.theme.Shad
import java.time.format.DateTimeFormatter

private val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")
private val ranges = listOf(7, 30)

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }
    val weekdays = stringResource(R.string.weekdays_short).split(",")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
                MutedText(
                    stringResource(R.string.history_average, (Math.round(state.averagePerDay * 10) / 10.0).toString()),
                )
            }
        }

        if (state.goal > 0 && state.trackedScores.isNotEmpty()) {
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.history_goal_title, state.goal))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.history_goal_days, state.withinGoal, state.trackedScores.size),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    MutedText(pluralStringResource(R.plurals.history_goal_streak, state.streak, state.streak))
                }
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.history_trend))
                Spacer(Modifier.height(16.dp))
                val first = state.days.firstOrNull()?.date
                val last = state.days.lastOrNull()?.date
                BarChart(
                    values = state.days.map { it.score },
                    axisLabels = listOfNotNull(first?.format(dayFormat), last?.format(dayFormat)),
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = stringResource(R.string.history_trend_desc, state.rangeDays, state.total),
                    goal = state.goal,
                )
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.history_heatmap))
                Spacer(Modifier.height(16.dp))
                Heatmap(
                    state.heatmap,
                    weekdays,
                    Modifier.fillMaxWidth(),
                    contentDescription = stringResource(R.string.history_heatmap),
                )
            }
        }

        if (state.topApps.isNotEmpty()) {
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.history_top_apps))
                    CardDescription(stringResource(R.string.history_range, state.rangeDays))
                    state.topApps.forEach { app ->
                        Spacer(Modifier.height(12.dp))
                        ShadSeparator()
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClickLabel = stringResource(R.string.open_app_details, app.label)) {
                                    onOpenApp(app.packageName)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                app.label,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            ShadBadge(app.score.toString(), variant = ShadBadgeVariant.Default)
                        }
                    }
                }
            }
        } else if (state.loaded) {
            item {
                ShadCard {
                    MutedText(
                        stringResource(R.string.history_empty),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
