package com.loopbreak.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.loopbreak.domain.detect.DetectionConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Everything the user can change in the Settings screen, plus small bits of notification state. */
data class UserSettings(
    val reOpenMinCount: Int = 2,
    val reOpenWindowMin: Int = 10,
    val quickPeekMaxSec: Int = 5,
    val excludedPackages: Set<String> = emptySet(),
    val notificationsEnabled: Boolean = false,
    val lastNotifiedAt: Long = 0L,
) {
    fun toConfig(): DetectionConfig = DetectionConfig(
        reOpenWindowMs = reOpenWindowMin * 60_000L,
        reOpenMinCount = reOpenMinCount,
        quickPeekMaxMs = quickPeekMaxSec * 1_000L,
    )
}

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val lastSync = longPreferencesKey("last_sync")
        val detectionVersion = intPreferencesKey("detection_version")
        val reOpenMinCount = intPreferencesKey("re_open_min_count")
        val reOpenWindowMin = intPreferencesKey("re_open_window_min")
        val quickPeekMaxSec = intPreferencesKey("quick_peek_max_sec")
        val excluded = stringSetPreferencesKey("excluded_packages")
        val notifications = booleanPreferencesKey("notifications_enabled")
        val lastNotifiedAt = longPreferencesKey("last_notified_at")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { it.toUserSettings() }

    suspend fun current(): UserSettings = settings.first()

    suspend fun update(transform: (UserSettings) -> UserSettings) {
        context.dataStore.edit { prefs ->
            val next = transform(prefs.toUserSettings())
            prefs[Keys.reOpenMinCount] = next.reOpenMinCount
            prefs[Keys.reOpenWindowMin] = next.reOpenWindowMin
            prefs[Keys.quickPeekMaxSec] = next.quickPeekMaxSec
            prefs[Keys.excluded] = next.excludedPackages
            prefs[Keys.notifications] = next.notificationsEnabled
            prefs[Keys.lastNotifiedAt] = next.lastNotifiedAt
        }
    }

    suspend fun detectionVersion(): Int = context.dataStore.data.first()[Keys.detectionVersion] ?: 0

    suspend fun setDetectionVersion(value: Int) {
        context.dataStore.edit { it[Keys.detectionVersion] = value }
    }

    suspend fun lastSync(): Long? = context.dataStore.data.first()[Keys.lastSync]

    suspend fun setLastSync(value: Long) {
        context.dataStore.edit { it[Keys.lastSync] = value }
    }

    suspend fun clearLastSync() {
        context.dataStore.edit { it.remove(Keys.lastSync) }
    }

    private fun Preferences.toUserSettings(): UserSettings {
        val d = UserSettings()
        return UserSettings(
            reOpenMinCount = this[Keys.reOpenMinCount] ?: d.reOpenMinCount,
            reOpenWindowMin = this[Keys.reOpenWindowMin] ?: d.reOpenWindowMin,
            quickPeekMaxSec = this[Keys.quickPeekMaxSec] ?: d.quickPeekMaxSec,
            excludedPackages = this[Keys.excluded] ?: d.excludedPackages,
            notificationsEnabled = this[Keys.notifications] ?: d.notificationsEnabled,
            lastNotifiedAt = this[Keys.lastNotifiedAt] ?: d.lastNotifiedAt,
        )
    }
}
