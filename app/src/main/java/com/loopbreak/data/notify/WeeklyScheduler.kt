package com.loopbreak.data.notify

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.loopbreak.domain.detect.WeeklySchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Turns the weekly summary on and off in WorkManager. */
@Singleton
class WeeklyScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val work get() = WorkManager.getInstance(context)

    /** Every 7 days, first run on the coming Sunday evening. Safe to call again. */
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(WeeklySchedule.delayUntilNext(ZonedDateTime.now()), TimeUnit.MILLISECONDS)
            .build()
        work.enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() {
        work.cancelUniqueWork(UNIQUE_NAME)
    }

    /** Sends the recap right now, whatever the setting says. */
    fun sendPreview() {
        val request = OneTimeWorkRequestBuilder<WeeklySummaryWorker>()
            .setInputData(Data.Builder().putBoolean(WeeklySummaryWorker.KEY_FORCE, true).build())
            .build()
        work.enqueue(request)
    }

    private companion object {
        const val UNIQUE_NAME = "weekly-summary"
    }
}
