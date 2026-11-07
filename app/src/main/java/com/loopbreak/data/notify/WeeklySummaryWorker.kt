package com.loopbreak.data.notify

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.loopbreak.data.LoopRepository
import com.loopbreak.data.settings.SettingsStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Sends the weekly recap. Scheduled by [WeeklyScheduler]; [KEY_FORCE] makes it ignore the setting (preview). */
@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LoopRepository,
    private val settings: SettingsStore,
    private val notifier: LoopNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val force = inputData.getBoolean(KEY_FORCE, false)
        if (!force && !settings.current().weeklySummaryEnabled) return Result.success()
        return try {
            notifier.postWeekly(repository.weeklySummary())
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_FORCE = "force"
    }
}
