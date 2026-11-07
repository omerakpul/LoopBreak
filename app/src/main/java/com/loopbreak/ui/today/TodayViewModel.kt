package com.loopbreak.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.settings.SettingsStore
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.detect.DayDelta
import com.loopbreak.domain.detect.LoopScore
import com.loopbreak.domain.detect.LoopScore.score
import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppLoopStat(
    val packageName: String,
    val label: String,
    val returns: Int,
    val peeks: Int,
    val pingPongs: Int,
) {
    val total: Int get() = returns + peeks
}

data class LoopUi(val event: LoopEvent, val title: String)

data class TodayUiState(
    val loaded: Boolean = false,
    /** 0 = today, -1 = yesterday, ... */
    val dayOffset: Int = 0,
    val day: LocalDate = LocalDate.now(),
    val score: Int = 0,
    /** Compared with the day before; null when both days are empty. */
    val delta: DayDelta? = null,
    /** Daily goal from the settings; 0 = off. */
    val goal: Int = 0,
    val hourly: List<Int> = List(24) { 0 },
    val topApps: List<AppLoopStat> = emptyList(),
    val loops: List<LoopUi> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    repository: LoopRepository,
    settings: SettingsStore,
    private val labels: AppLabels,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val _dayOffset = MutableStateFlow(0)
    val dayOffset = _dayOffset.asStateFlow()
    private val dayTick = MutableStateFlow(0)

    val state: StateFlow<TodayUiState> = combine(_dayOffset, dayTick) { offset, _ -> offset }
        .flatMapLatest { offset ->
            val day = LocalDate.now(zone).plusDays(offset.toLong())
            val from = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val previousFrom = day.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            // One query for both days; the previous day only feeds the comparison badge.
            repository.observeLoops(previousFrom, to).map { build(it, offset, day, from) }
        }
        .flowOn(Dispatchers.Default)
        .combine(settings.settings.map { it.dailyGoal }.distinctUntilChanged()) { s, goal -> s.copy(goal = goal) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun previousDay() {
        _dayOffset.value -= 1
    }

    /** Re-evaluates "today" (e.g. after midnight passed while the app was in the background). */
    fun refreshDay() {
        dayTick.value += 1
    }

    fun nextDay() {
        if (_dayOffset.value < 0) _dayOffset.value += 1
    }

    private fun build(all: List<LoopEvent>, offset: Int, day: LocalDate, dayStart: Long): TodayUiState {
        val loops = all.filter { it.startTime >= dayStart }
        val previous = all.filter { it.startTime < dayStart }
        val hourly = IntArray(24)
        for (l in loops) {
            val hour = Instant.ofEpochMilli(l.startTime).atZone(zone).hour
            hourly[hour] += l.score()
        }

        val perApp = loops.groupBy { it.packageName }.map { (pkg, list) ->
            AppLoopStat(
                packageName = pkg,
                label = labels.label(pkg),
                returns = list.filter { it.type == LoopType.RE_OPEN }.sumOf { it.score() },
                peeks = list.count { it.type == LoopType.QUICK_PEEK },
                pingPongs = list.count { it.type == LoopType.PING_PONG },
            )
        }.filter { it.total > 0 || it.pingPongs > 0 }
            .sortedWith(compareByDescending<AppLoopStat> { it.total }.thenByDescending { it.pingPongs })

        val rows = loops.sortedByDescending { it.startTime }.map { event ->
            val title = if (event.type == LoopType.PING_PONG && event.otherPackage != null) {
                "${labels.label(event.packageName)} ↔ ${labels.label(event.otherPackage)}"
            } else {
                labels.label(event.packageName)
            }
            LoopUi(event, title)
        }

        return TodayUiState(
            loaded = true,
            dayOffset = offset,
            day = day,
            score = LoopScore.of(loops),
            delta = DayDelta.of(LoopScore.of(loops), LoopScore.of(previous)),
            hourly = hourly.toList(),
            topApps = perApp,
            loops = rows,
        )
    }
}
