package com.loopbreak.di

import android.content.Context
import androidx.room.Room
import com.loopbreak.data.db.LoopDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): LoopDatabase =
        Room.databaseBuilder(context, LoopDatabase::class.java, "loopbreak.db").build()
}
