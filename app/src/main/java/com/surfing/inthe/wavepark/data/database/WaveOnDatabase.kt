package com.surfing.inthe.wavepark.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.surfing.inthe.wavepark.data.database.dao.ReservationDao
import com.surfing.inthe.wavepark.data.database.dao.EventDao
import com.surfing.inthe.wavepark.data.database.dao.WeatherDataDao
import com.surfing.inthe.wavepark.data.database.dao.TemperatureDataDao
import com.surfing.inthe.wavepark.data.database.entity.ReservationEntity
import com.surfing.inthe.wavepark.data.database.entity.EventEntity
import com.surfing.inthe.wavepark.data.database.entity.WeatherDataEntity
import com.surfing.inthe.wavepark.data.database.entity.TemperatureDataEntity
import com.surfing.inthe.wavepark.data.database.converter.DateConverter

@Database(
    entities = [
        ReservationEntity::class,
        EventEntity::class,
        WeatherDataEntity::class,
        TemperatureDataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class WaveOnDatabase : RoomDatabase() {
    
    abstract fun reservationDao(): ReservationDao
    abstract fun eventDao(): EventDao
    abstract fun weatherDataDao(): WeatherDataDao
    abstract fun temperatureDataDao(): TemperatureDataDao

    companion object {
        @Volatile
        private var INSTANCE: WaveOnDatabase? = null

        fun getDatabase(context: Context): WaveOnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaveOnDatabase::class.java,
                    "waveon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 