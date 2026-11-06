package com.loopbreak.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.model.UsageEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

data class RawEventUi(val label: String, val type: UsageEventType?, val timestamp: Long)

data class DebugUiState(
    val totalCount: Int = 0,
    val recent: List<RawEventUi> = emptyList(),
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    repository: LoopRepository,
    private val labels: AppLabels,
) : ViewModel() {

    val state: StateFlow<DebugUiState> = combine(
        repository.observeEventCount(),
        repository.observeRecentEvents(200),
    ) { count, events ->
        DebugUiState(
            totalCount = count,
            recent = events.map {
                RawEventUi(labels.label(it.packageName), UsageEventType.fromCode(it.type), it.timestamp)
            },
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugUiState())
}
