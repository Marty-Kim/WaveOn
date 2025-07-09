package com.surfing.inthe.wavepark.data.database.dao

import androidx.room.*
import com.surfing.inthe.wavepark.data.database.entity.WeatherDataEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WeatherDataDao {
    
    @Query("SELECT * FROM weather_data ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeatherData(): WeatherDataEntity?
    
    @Query("SELECT * FROM weather_data WHERE date >= :startDate ORDER BY date ASC")
    fun getWeatherDataFromDate(startDate: Date): Flow<List<WeatherDataEntity>>
    
    @Query("SELECT * FROM weather_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeatherDataBetweenDates(startDate: Date, endDate: Date): Flow<List<WeatherDataEntity>>
    
    @Query("SELECT * FROM weather_data WHERE id = :id")
    suspend fun getWeatherDataById(id: Long): WeatherDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherData: WeatherDataEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherDataList(weatherDataList: List<WeatherDataEntity>)
    
    @Update
    suspend fun updateWeatherData(weatherData: WeatherDataEntity)
    
    @Delete
    suspend fun deleteWeatherData(weatherData: WeatherDataEntity)
    
    @Query("DELETE FROM weather_data WHERE date < :date")
    suspend fun deleteOldWeatherData(date: Date)
    
    @Query("DELETE FROM weather_data")
    suspend fun deleteAllWeatherData()
} 