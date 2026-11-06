package com.loopbreak.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import com.loopbreak.domain.model.UsageEvent
import com.loopbreak.domain.model.UsageEventType

@Entity(
    tableName = "usage_events",
    indices = [
        Index(value = ["packageName", "type", "timestamp"], unique = true),
        Index(value = ["timestamp"]),
    ],
)
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val type: Int,
    val timestamp: Long,
)

@Entity(
    tableName = "loop_events",
    indices = [Index(value = ["startTime"])],
)
data class LoopEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Int,
    val packageName: String,
    val otherPackage: String?,
    val startTime: Long,
    val endTime: Long,
    val count: Int,
)

fun UsageEvent.toEntity() = UsageEventEntity(
    packageName = packageName,
    type = type.code,
    timestamp = timestamp,
)

fun UsageEventEntity.toDomain(): UsageEvent? =
    UsageEventType.fromCode(type)?.let { UsageEvent(packageName, it, timestamp) }

fun LoopEvent.toEntity() = LoopEventEntity(
    type = type.ordinal,
    packageName = packageName,
    otherPackage = otherPackage,
    startTime = startTime,
    endTime = endTime,
    count = count,
)

fun LoopEventEntity.toDomain() = LoopEvent(
    type = LoopType.entries[type],
    packageName = packageName,
    otherPackage = otherPackage,
    startTime = startTime,
    endTime = endTime,
    count = count,
)
