package com.loopbreak.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.loopbreak.domain.model.UsageEvent
import com.loopbreak.domain.model.UsageEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin wrapper around UsageStatsManager. Everything Android-specific about reading events lives here. */
@Singleton
class UsageStatsReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Suppress("DEPRECATION")
    fun hasPermission(): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            ops.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Foreground/background events in [from, to), oldest first. */
    @Suppress("DEPRECATION")
    fun read(from: Long, to: Long): List<UsageEvent> {
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return emptyList()
        val events = manager.queryEvents(from, to) ?: return emptyList()
        val out = mutableListOf<UsageEvent>()
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            // MOVE_TO_FOREGROUND/BACKGROUND share their values with ACTIVITY_RESUMED/PAUSED (API 29+).
            val type = when (e.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> UsageEventType.RESUMED
                UsageEvents.Event.MOVE_TO_BACKGROUND -> UsageEventType.PAUSED
                else -> continue
            }
            out += UsageEvent(e.packageName, type, e.timeStamp)
        }
        return out
    }
}
