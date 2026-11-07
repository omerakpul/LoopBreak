package com.loopbreak.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<UsageEventEntity>)

    @Query("SELECT * FROM usage_events WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp, id")
    suspend fun between(from: Long, to: Long): List<UsageEventEntity>

    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<UsageEventEntity>>

    @Query("SELECT MIN(timestamp) FROM usage_events")
    suspend fun earliest(): Long?

    @Query("SELECT DISTINCT packageName FROM usage_events WHERE timestamp >= :since")
    suspend fun packagesSince(since: Long): List<String>

    @Query("SELECT COUNT(*) FROM usage_events")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM usage_events WHERE timestamp < :before")
    suspend fun deleteBefore(before: Long)

    @Query("DELETE FROM usage_events")
    suspend fun deleteAll()
}

@Dao
interface LoopEventDao {
    @Insert
    suspend fun insertAll(events: List<LoopEventEntity>)

    @Query("DELETE FROM loop_events WHERE startTime >= :from")
    suspend fun deleteFrom(from: Long)

    @Query("DELETE FROM loop_events WHERE startTime < :before")
    suspend fun deleteBefore(before: Long)

    @Query("DELETE FROM loop_events")
    suspend fun deleteAll()

    @Query("SELECT * FROM loop_events WHERE startTime >= :from AND startTime < :to ORDER BY startTime")
    fun observeBetween(from: Long, to: Long): Flow<List<LoopEventEntity>>

    @Query(
        "SELECT * FROM loop_events WHERE (packageName = :packageName OR otherPackage = :packageName) " +
            "AND startTime >= :from AND startTime < :to ORDER BY startTime",
    )
    fun observeForPackage(packageName: String, from: Long, to: Long): Flow<List<LoopEventEntity>>
}
