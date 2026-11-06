package com.loopbreak.data

import androidx.room.withTransaction
import com.loopbreak.data.db.LoopDatabase
import com.loopbreak.data.db.UsageEventEntity
import com.loopbreak.data.db.toDomain
import com.loopbreak.data.db.toEntity
import com.loopbreak.data.notify.LoopNotifier
import com.loopbreak.data.settings.SettingsStore
import com.loopbreak.data.usage.ExcludedPackages
import com.loopbreak.data.usage.UsageStatsReader
import com.loopbreak.domain.detect.CalibrationStats
import com.loopbreak.domain.detect.LoopDetector
import com.loopbreak.domain.detect.SessionBuilder
import com.loopbreak.domain.model.LoopEvent
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface SyncResult {
    data object NoPermission : SyncResult
    data class Done(val newEvents: Int) : SyncResult
}

@Singleton
class LoopRepository @Inject constructor(
    private val db: LoopDatabase,
    private val reader: UsageStatsReader,
    private val excluded: ExcludedPackages,
    private val settings: SettingsStore,
    private val notifier: LoopNotifier,
) {
    private val usageDao = db.usageEventDao()
    private val loopDao = db.loopEventDao()
    private val mutex = Mutex()

    fun hasPermission(): Boolean = reader.hasPermission()

    fun observeLoops(from: Long, to: Long): Flow<List<LoopEvent>> =
        loopDao.observeBetween(from, to).map { list -> list.map { it.toDomain() } }

    fun observeRecentEvents(limit: Int): Flow<List<UsageEventEntity>> = usageDao.observeRecent(limit)

    fun observeEventCount(): Flow<Int> = usageDao.observeCount()

    /** Apps seen recently that the user could choose to exclude (system defaults are left out). */
    suspend fun candidatePackages(now: Long = System.currentTimeMillis()): List<String> =
        withContext(Dispatchers.IO) {
            val skip = excluded.systemDefaults()
            usageDao.packagesSince(now - 14 * DAY_MS).filterNot { it in skip }
        }

    /** Session length and return-gap distribution for the last [days] days, for threshold tuning. */
    suspend fun calibrationStats(days: Int = 7, now: Long = System.currentTimeMillis()): CalibrationStats =
        withContext(Dispatchers.IO) {
            val s = settings.current()
            val events = usageDao.between(now - days * DAY_MS, now + 1).mapNotNull { it.toDomain() }
            val sessions = SessionBuilder(s.toConfig()).build(events, excluded.get(s.excludedPackages), openUntil = now)
            CalibrationStats.from(sessions)
        }

    /**
     * Copies new system events into our database and recomputes loops from shortly before the
     * start of the day of the earliest new event. Safe to call often and from several places.
     */
    suspend fun sync(now: Long = System.currentTimeMillis()): SyncResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!reader.hasPermission()) return@withLock SyncResult.NoPermission

            val last = settings.lastSync()
            val from = if (last == null) now - FIRST_SYNC_LOOKBACK_MS else last - SYNC_OVERLAP_MS
            val events = reader.read(from, now)
            if (events.isNotEmpty()) usageDao.insertAll(events.map { it.toEntity() })

            events.minOfOrNull { it.timestamp }?.let { earliest ->
                val loops = rebuild(startOfDay(earliest) - MIDNIGHT_BUFFER_MS, now)
                notifier.check(loops, now)
            }
            // Default exclusions or detection rules changed in this build: redo stored history once.
            if (settings.detectionVersion() != DETECTION_VERSION) {
                usageDao.earliest()?.let { rebuild(startOfDay(it) - MIDNIGHT_BUFFER_MS, now) }
                settings.setDetectionVersion(DETECTION_VERSION)
            }
            settings.setLastSync(now)
            SyncResult.Done(events.size)
        }
    }

    /** Recomputes every stored loop, e.g. after the user changed thresholds or the exclusion list. */
    suspend fun recomputeAll(now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val earliest = usageDao.earliest() ?: return@withLock
            rebuild(startOfDay(earliest) - MIDNIGHT_BUFFER_MS, now)
        }
    }

    /** Throws away stored data and starts over from whatever the system still has. */
    suspend fun resetAndResync(now: Long = System.currentTimeMillis()): SyncResult =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                clearAll()
                settings.clearLastSync()
            }
            sync(now)
        }

    /** Deletes everything we stored. Collection restarts from "now", old system events are not re-imported. */
    suspend fun deleteAllData(now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        mutex.withLock {
            clearAll()
            settings.setLastSync(now)
        }
    }

    private suspend fun clearAll() {
        db.withTransaction {
            usageDao.deleteAll()
            loopDao.deleteAll()
        }
    }

    /** Returns the loops that were computed for [from, now]. */
    private suspend fun rebuild(from: Long, now: Long): List<LoopEvent> {
        val config = settings.current().toConfig()
        val events = usageDao.between(from, now + 1).mapNotNull { it.toDomain() }
        val skip = excluded.get(settings.current().excludedPackages)
        val sessions = SessionBuilder(config).build(events, skip, openUntil = now)
        val loops = LoopDetector(config).detect(sessions)
        db.withTransaction {
            loopDao.deleteFrom(from)
            loopDao.insertAll(loops.map { it.toEntity() })
        }
        return loops
    }

    companion object {
        /** Bump whenever default exclusions or detection rules change so stored loops get recomputed. */
        private const val DETECTION_VERSION = 2

        const val DAY_MS = 24 * 60 * 60_000L
        private const val FIRST_SYNC_LOOKBACK_MS = 7 * DAY_MS
        private const val SYNC_OVERLAP_MS = 2 * 60_000L

        /** Recompute a bit before midnight so a loop that straddles the day boundary is not cut. */
        private const val MIDNIGHT_BUFFER_MS = 60 * 60_000L

        fun startOfDay(timeMs: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
            Instant.ofEpochMilli(timeMs).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
