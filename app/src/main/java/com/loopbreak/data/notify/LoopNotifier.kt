package com.loopbreak.data.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.loopbreak.MainActivity
import com.loopbreak.R
import com.loopbreak.data.settings.SettingsStore
import com.loopbreak.data.usage.AppLabels
import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Shows one gentle nudge when the same app keeps pulling you back. Off by default. */
@Singleton
class LoopNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsStore,
    private val labels: AppLabels,
) {
    fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * @param loops freshly computed loops; only recent re-open clusters that we have not
     * announced yet trigger a notification.
     */
    suspend fun check(loops: List<LoopEvent>, now: Long) {
        val s = settings.current()
        if (!s.notificationsEnabled || !canPost()) return

        val hit = loops
            .filter { it.type == LoopType.RE_OPEN && it.count >= NOTIFY_MIN_COUNT }
            .filter { it.endTime > s.lastNotifiedAt && now - it.endTime <= RECENT_MS }
            .maxByOrNull { it.endTime } ?: return

        post(labels.label(hit.packageName), hit.count, s.reOpenWindowMin)
        settings.update { it.copy(lastNotifiedAt = hit.endTime) }
    }

    private fun post(appLabel: String, count: Int, windowMin: Int) {
        ensureChannel()
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text, appLabel, count, windowMin))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call: nothing to do.
        }
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "loops"
        const val NOTIFICATION_ID = 1
        const val NOTIFY_MIN_COUNT = 3
        const val RECENT_MS = 10 * 60_000L
    }
}
