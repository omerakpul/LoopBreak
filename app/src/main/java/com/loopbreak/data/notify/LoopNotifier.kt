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
import com.loopbreak.domain.detect.DayDelta
import com.loopbreak.domain.detect.WeeklySummary
import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** All notifications LoopBreak can show. Both kinds are opt-in and off by default. */
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
     * A gentle nudge when the same app keeps pulling you back.
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

        post(
            channelId = NUDGE_CHANNEL,
            channelName = R.string.notification_channel,
            id = NUDGE_ID,
            title = context.getString(R.string.notification_title),
            text = context.getString(R.string.notification_text, labels.label(hit.packageName), hit.count, s.reOpenWindowMin),
        )
        settings.update { it.copy(lastNotifiedAt = hit.endTime) }
    }

    /** The weekly recap: score, change against the week before, and the app with the most loops. */
    fun postWeekly(summary: WeeklySummary) {
        if (!canPost() || summary.isEmpty) return
        val text = if (summary.total == 0) {
            context.getString(R.string.weekly_text_zero)
        } else {
            context.getString(
                R.string.weekly_text,
                summary.total,
                deltaText(summary.delta),
                summary.topPackage?.let(labels::label).orEmpty(),
            )
        }
        post(
            channelId = WEEKLY_CHANNEL,
            channelName = R.string.weekly_channel,
            id = WEEKLY_ID,
            title = context.getString(R.string.weekly_title),
            text = text,
        )
    }

    private fun deltaText(delta: DayDelta?): String = when {
        delta == null || delta.delta == 0 -> context.getString(R.string.weekly_delta_same)
        delta.isBetter -> context.getString(R.string.weekly_delta_down, delta.percent ?: 0)
        delta.percent != null -> context.getString(R.string.weekly_delta_up_pct, delta.percent)
        else -> context.getString(R.string.weekly_delta_up_abs, delta.delta)
    }

    private fun post(channelId: String, channelName: Int, id: Int, title: String, text: String) {
        ensureChannel(channelId, channelName)
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call: nothing to do.
        }
    }

    private fun ensureChannel(channelId: String, name: Int) {
        val channel = NotificationChannel(channelId, context.getString(name), NotificationManager.IMPORTANCE_LOW)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val NUDGE_CHANNEL = "loops"
        const val WEEKLY_CHANNEL = "weekly"
        const val NUDGE_ID = 1
        const val WEEKLY_ID = 2
        const val NOTIFY_MIN_COUNT = 3
        const val RECENT_MS = 10 * 60_000L
    }
}
