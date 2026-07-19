package com.example.livebus.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineTransitModule {

    @Provides
    @Singleton
    fun provideOfflineTransitDatabase(@ApplicationContext context: Context): OfflineTransitDatabase {
        return OfflineTransitDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideOfflineTransitDao(database: OfflineTransitDatabase): OfflineTransitDao {
        return database.offlineTransitDao()
    }
}
