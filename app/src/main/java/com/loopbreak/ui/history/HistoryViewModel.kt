package com.loopbreak.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.settings.SettingsStore
import com.loopbreak.domain.detect.GoalStreak
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.detect.LoopScore.score
import com.loopbreak.domain.model.LoopEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DayScore(val date: LocalDate, val score: Int)

data class TopApp(val packageName: String, val label: String, val score: Int)

data class HistoryUiState(
    val rangeDays: Int = 7,
    val loaded: Boolean = false,
    val days: List<DayScore> = emptyList(),
    /** 7 rows (Monday..Sunday) x 24 hours. */
    val heatmap: List<List<Int>> = List(7) { List(24) { 0 } },
    val topApps: List<TopApp> = emptyList(),
    val total: Int = 0,
    val averagePerDay: Float = 0f,
    /** Scores of the days that were actually tracked, oldest first, today last. */
    val trackedScores: List<Int> = emptyList(),
    val goal: Int = 0,
    val withinGoal: Int = 0,
    val streak: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: LoopRepository,
    settings: SettingsStore,
    private val labels: AppLabels,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val _rangeDays = MutableStateFlow(7)
    private val dayTick = MutableStateFlow(0)

    val state: StateFlow<HistoryUiState> = combine(_rangeDays, dayTick) { n, _ -> n }
        .flatMapLatest { n ->
            val today = LocalDate.now(zone)
            val firstDay = today.minusDays(n - 1L)
            val from = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val firstTracked = repository.firstTrackedDay(zone)
            repository.observeLoops(from, to).map { build(n, firstDay, it, firstTracked) }
        }
        .flowOn(Dispatchers.Default)
        .combine(settings.settings.map { it.dailyGoal }.distinctUntilChanged()) { s, goal ->
            s.copy(
                goal = goal,
                withinGoal = GoalStreak.daysWithin(s.trackedScores, goal),
                streak = GoalStreak.current(s.trackedScores, goal),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    val rangeDays: StateFlow<Int> = _rangeDays.asStateFlow()

    fun refreshDay() {
        dayTick.value += 1
    }

    fun setRange(days: Int) {
        _rangeDays.value = days
    }

    private fun build(n: Int, firstDay: LocalDate, loops: List<LoopEvent>, firstTracked: LocalDate?): HistoryUiState {
        val byDay = IntArray(n)
        val heat = Array(7) { IntArray(24) }
        val perApp = HashMap<String, Int>()

        for (l in loops) {
            val at = Instant.ofEpochMilli(l.startTime).atZone(zone)
            val index = (at.toLocalDate().toEpochDay() - firstDay.toEpochDay()).toInt()
            val s = l.score()
            if (index in 0 until n) byDay[index] += s
            heat[at.dayOfWeek.value - 1][at.hour] += s
            if (s > 0) perApp.merge(l.packageName, s, Int::plus)
        }

        val total = byDay.sum()
        return HistoryUiState(
            rangeDays = n,
            loaded = true,
            days = byDay.mapIndexed { i, v -> DayScore(firstDay.plusDays(i.toLong()), v) },
            heatmap = heat.map { it.toList() },
            topApps = perApp.entries
                .sortedByDescending { it.value }
                .take(10)
                .map { TopApp(it.key, labels.label(it.key), it.value) },
            total = total,
            averagePerDay = total / n.toFloat(),
            trackedScores = if (firstTracked == null) emptyList() else
                byDay.filterIndexed { i, _ -> !firstDay.plusDays(i.toLong()).isBefore(firstTracked) },
        )
    }
}
