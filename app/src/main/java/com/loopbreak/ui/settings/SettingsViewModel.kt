package com.loopbreak.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.notify.LoopNotifier
import com.loopbreak.data.settings.SettingsStore
import com.loopbreak.data.settings.UserSettings
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.detect.CalibrationStats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Candidate(val packageName: String, val label: String, val excluded: Boolean)

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val candidates: List<Candidate> = emptyList(),
    val calibration: CalibrationStats? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore,
    private val repository: LoopRepository,
    private val notifier: LoopNotifier,
    private val labels: AppLabels,
) : ViewModel() {

    private val candidatePackages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    private val calibration = MutableStateFlow<CalibrationStats?>(null)

    val state: StateFlow<SettingsUiState> = combine(store.settings, candidatePackages, calibration) { s, apps, stats ->
        SettingsUiState(
            settings = s,
            candidates = apps.map { (pkg, label) -> Candidate(pkg, label, pkg in s.excludedPackages) },
            calibration = stats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.Default) {
                repository.candidatePackages()
                    .map { it to labels.label(it) }
                    .sortedBy { it.second.lowercase() }
            }
            candidatePackages.value = apps
        }
        loadCalibration()
    }

    fun canPostNotifications(): Boolean = notifier.canPost()

    fun setReOpenMinCount(value: Int) = change { it.copy(reOpenMinCount = value.coerceIn(2, 10)) }
    fun setReOpenWindowMin(value: Int) = change { it.copy(reOpenWindowMin = value.coerceIn(1, 60)) }
    fun setQuickPeekMaxSec(value: Int) = change { it.copy(quickPeekMaxSec = value.coerceIn(1, 30)) }

    fun setExcluded(packageName: String, excluded: Boolean) = change {
        it.copy(excludedPackages = if (excluded) it.excludedPackages + packageName else it.excludedPackages - packageName)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { store.update { it.copy(notificationsEnabled = enabled) } }
    }

    fun deleteAllData() {
        viewModelScope.launch { repository.deleteAllData() }
    }

    private fun loadCalibration() {
        viewModelScope.launch { calibration.value = repository.calibrationStats() }
    }

    /** Detection settings affect already stored data, so everything is recomputed after a change. */
    private fun change(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            store.update(transform)
            repository.recomputeAll()
            loadCalibration()
        }
    }
}
