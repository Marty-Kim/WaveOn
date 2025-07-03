package com.surfing.inthe.wavepark.data.database.dao

import androidx.room.*
import com.surfing.inthe.wavepark.data.database.entity.TemperatureDataEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TemperatureDataDao {
    
    @Query("SELECT * FROM temperature_data ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTemperatureData(): TemperatureDataEntity?
    
    @Query("SELECT * FROM temperature_data WHERE date >= :startDate ORDER BY date ASC")
    fun getTemperatureDataFromDate(startDate: Date): Flow<List<TemperatureDataEntity>>
    
    @Query("SELECT * FROM temperature_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getTemperatureDataBetweenDates(startDate: Date, endDate: Date): Flow<List<TemperatureDataEntity>>
    
    @Query("SELECT * FROM temperature_data WHERE location = :location ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTemperatureDataByLocation(location: String): TemperatureDataEntity?
    
    @Query("SELECT * FROM temperature_data WHERE id = :id")
    suspend fun getTemperatureDataById(id: Long): TemperatureDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureData(temperatureData: TemperatureDataEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureDataList(temperatureDataList: List<TemperatureDataEntity>)
    
    @Update
    suspend fun updateTemperatureData(temperatureData: TemperatureDataEntity)
    
    @Delete
    suspend fun deleteTemperatureData(temperatureData: TemperatureDataEntity)
    
    @Query("DELETE FROM temperature_data WHERE date < :date")
    suspend fun deleteOldTemperatureData(date: Date)
    
    @Query("DELETE FROM temperature_data")
    suspend fun deleteAllTemperatureData()
} 