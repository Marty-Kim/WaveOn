package com.surfing.inthe.wavepark.data.repository

import com.surfing.inthe.wavepark.data.api.WaterTemperatureApiService
import com.surfing.inthe.wavepark.data.database.dao.TemperatureDataDao
import com.surfing.inthe.wavepark.data.database.entity.TemperatureDataEntity
import com.surfing.inthe.wavepark.data.model.TemperatureData
import com.surfing.inthe.wavepark.data.model.TemperatureResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureRepository @Inject constructor(
    private val waterTemperatureApiService: WaterTemperatureApiService,
    private val temperatureDataDao: TemperatureDataDao
) {
    
    // RoomDB에서 최신 수온 데이터 가져오기
    suspend fun getLatestTemperatureData(): TemperatureData? {
        return temperatureDataDao.getLatestTemperatureData()?.toDomainModel()
    }

    // RoomDB에서 수온 데이터 Flow 가져오기
    fun getTemperatureDataFromDate(startDate: Date): Flow<List<TemperatureData>> {
        return temperatureDataDao.getTemperatureDataFromDate(startDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun fetchTemperatureData(): TemperatureResult = withContext(Dispatchers.IO) {
        try {
            val response = waterTemperatureApiService.getWaterTemperature()
            
            val temperatureData = TemperatureData(
                temperature = response.temperature,
                humidity = response.humidity,
                waterTemperature = response.water_temperature,
                weather = null, // 날씨는 별도 API에서 가져옴
                recommendedWax = getRecommendedWax(response.water_temperature),
                timestamp = response.timestamp
            )
            
            // RoomDB에 수온 데이터 저장
            saveTemperatureData(temperatureData)
            
            TemperatureResult.Success(temperatureData)
            
        } catch (e: Exception) {
            TemperatureResult.Error("수온 API 호출 실패: ${e.message}")
        }
    }

    // RoomDB에 수온 데이터 저장
    private suspend fun saveTemperatureData(temperatureData: TemperatureData) {
        val entity = temperatureData.toEntity()
        temperatureDataDao.insertTemperatureData(entity)
    }
    
    private fun getRecommendedWax(waterTemp: Double?): String {
        if (waterTemp == null) return "-"
        
        return when {
            waterTemp <= 15 -> "COLD"
            waterTemp > 15 && waterTemp <= 20 -> "COOL"
            waterTemp > 20 && waterTemp <= 24 -> "WARM"
            else -> "TROPIC"
        }
    }

    // Entity를 도메인 모델로 변환하는 확장 함수
    private fun TemperatureDataEntity.toDomainModel(): TemperatureData {
        return TemperatureData(
            temperature = airTemperature,
            humidity = 0, // Entity에 없는 값
            waterTemperature = waterTemperature,
            weather = null, // Entity에 없는 값
            recommendedWax = getRecommendedWax(waterTemperature),
            timestamp = date.time
        )
    }

    // 도메인 모델을 Entity로 변환하는 확장 함수
    private fun TemperatureData.toEntity(): TemperatureDataEntity {
        return TemperatureDataEntity(
            date = Date(timestamp),
            waterTemperature = waterTemperature ?: 0.0,
            airTemperature = temperature ?: 0.0,
            location = "WavePark" // 기본 위치
        )
    }
} 