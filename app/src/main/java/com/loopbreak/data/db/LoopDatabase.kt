package com.loopbreak.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UsageEventEntity::class, LoopEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LoopDatabase : RoomDatabase() {
    abstract fun usageEventDao(): UsageEventDao
    abstract fun loopEventDao(): LoopEventDao
}
