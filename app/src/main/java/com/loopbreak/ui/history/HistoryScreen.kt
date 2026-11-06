package com.loopbreak.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.ui.components.BarChart
import com.loopbreak.ui.components.Heatmap
import java.time.format.DateTimeFormatter

private val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weekdays = stringResource(R.string.weekdays_short).split(",")

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30).forEach { days ->
                    FilterChip(
                        selected = state.rangeDays == days,
                        onClick = { viewModel.setRange(days) },
                        label = { Text(stringResource(R.string.history_range, days)) },
                    )
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.history_total), style = MaterialTheme.typography.labelLarge)
                    Text(state.total.toString(), style = MaterialTheme.typography.displayMedium)
                    Text(
                        stringResource(R.string.history_average, (Math.round(state.averagePerDay * 10) / 10.0).toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { Text(stringResource(R.string.history_trend), style = MaterialTheme.typography.titleMedium) }
        item {
            val first = state.days.firstOrNull()?.date
            val last = state.days.lastOrNull()?.date
            BarChart(
                values = state.days.map { it.score },
                axisLabels = listOfNotNull(first?.format(dayFormat), last?.format(dayFormat)),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item { Text(stringResource(R.string.history_heatmap), style = MaterialTheme.typography.titleMedium) }
        item { Heatmap(state.heatmap, weekdays, Modifier.fillMaxWidth()) }

        if (state.topApps.isNotEmpty()) {
            item { Text(stringResource(R.string.history_top_apps), style = MaterialTheme.typography.titleMedium) }
            items(state.topApps, key = { it.label }) { app ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(app.label, style = MaterialTheme.typography.bodyLarge)
                    Text(app.score.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else if (state.loaded) {
            item {
                Text(
                    stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
