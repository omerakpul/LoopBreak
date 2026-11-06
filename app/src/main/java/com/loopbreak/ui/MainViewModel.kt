package com.loopbreak.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopbreak.data.LoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncUiState(
    val syncing: Boolean = false,
    /** True when the last attempt threw; cleared by the next successful sync. */
    val failed: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LoopRepository,
) : ViewModel() {

    private val _hasPermission = MutableStateFlow(repository.hasPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _sync = MutableStateFlow(SyncUiState())
    val sync: StateFlow<SyncUiState> = _sync.asStateFlow()

    /** Called every time the app comes to the foreground: re-check permission and pull fresh events. */
    fun onResume() {
        val granted = repository.hasPermission()
        _hasPermission.value = granted
        if (granted) refresh()
    }

    fun refresh() = runSync { repository.sync() }

    fun resetAndResync() = runSync { repository.resetAndResync() }

    private fun runSync(block: suspend () -> Unit) {
        viewModelScope.launch {
            _sync.update { it.copy(syncing = true) }
            try {
                block()
                _sync.value = SyncUiState()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _sync.value = SyncUiState(failed = true)
            }
        }
    }
}
