package com.surfing.inthe.wavepark.di

import android.content.Context
import com.surfing.inthe.wavepark.data.database.WaveOnDatabase
import com.surfing.inthe.wavepark.data.database.dao.ReservationDao
import com.surfing.inthe.wavepark.data.database.dao.EventDao
import com.surfing.inthe.wavepark.data.database.dao.WeatherDataDao
import com.surfing.inthe.wavepark.data.database.dao.TemperatureDataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WaveOnDatabase {
        return WaveOnDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideReservationDao(database: WaveOnDatabase): ReservationDao {
        return database.reservationDao()
    }
    
    @Provides
    @Singleton
    fun provideEventDao(database: WaveOnDatabase): EventDao {
        return database.eventDao()
    }
    
    @Provides
    @Singleton
    fun provideWeatherDataDao(database: WaveOnDatabase): WeatherDataDao {
        return database.weatherDataDao()
    }
    
    @Provides
    @Singleton
    fun provideTemperatureDataDao(database: WaveOnDatabase): TemperatureDataDao {
        return database.temperatureDataDao()
    }
} 