package com.loopbreak.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.detect.LoopScore
import com.loopbreak.domain.detect.LoopScore.score
import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import com.loopbreak.ui.history.DayScore
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** One row of the recent-loops list; [partner] is the other app of a ping-pong. */
data class RecentLoop(val event: LoopEvent, val partner: String?)

data class AppDetailUiState(
    val rangeDays: Int = 7,
    val loaded: Boolean = false,
    val total: Int = 0,
    val returns: Int = 0,
    val peeks: Int = 0,
    val pingPongs: Int = 0,
    val days: List<DayScore> = emptyList(),
    val hourly: List<Int> = List(24) { 0 },
    val recent: List<RecentLoop> = emptyList(),
)

/** Everything LoopBreak knows about one app. The package name arrives as the "pkg" nav argument. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LoopRepository,
    private val labels: AppLabels,
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["pkg"])
    private val zone = ZoneId.systemDefault()
    private val _rangeDays = MutableStateFlow(7)
    private val dayTick = MutableStateFlow(0)

    val title: String = labels.label(packageName)

    val state: StateFlow<AppDetailUiState> = combine(_rangeDays, dayTick) { n, _ -> n }
        .flatMapLatest { n ->
            val today = LocalDate.now(zone)
            val firstDay = today.minusDays(n - 1L)
            val from = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            repository.observeLoopsForPackage(packageName, from, to).map { build(n, firstDay, it) }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppDetailUiState())

    fun setRange(days: Int) {
        _rangeDays.value = days
    }

    fun refreshDay() {
        dayTick.value += 1
    }

    private fun build(n: Int, firstDay: LocalDate, loops: List<LoopEvent>): AppDetailUiState {
        val byDay = IntArray(n)
        val hourly = IntArray(24)
        for (l in loops) {
            val at = Instant.ofEpochMilli(l.startTime).atZone(zone)
            val index = (at.toLocalDate().toEpochDay() - firstDay.toEpochDay()).toInt()
            val s = l.score()
            if (index in 0 until n) byDay[index] += s
            hourly[at.hour] += s
        }

        val recent = loops.sortedByDescending { it.startTime }.take(30).map { e ->
            val partner = if (e.type == LoopType.PING_PONG) {
                val other = if (e.packageName == packageName) e.otherPackage else e.packageName
                other?.let(labels::label)
            } else {
                null
            }
            RecentLoop(e, partner)
        }

        return AppDetailUiState(
            rangeDays = n,
            loaded = true,
            total = LoopScore.of(loops),
            returns = loops.filter { it.type == LoopType.RE_OPEN }.sumOf { it.score() },
            peeks = loops.count { it.type == LoopType.QUICK_PEEK },
            pingPongs = loops.count { it.type == LoopType.PING_PONG },
            days = byDay.mapIndexed { i, v -> DayScore(firstDay.plusDays(i.toLong()), v) },
            hourly = hourly.toList(),
            recent = recent,
        )
    }
}
