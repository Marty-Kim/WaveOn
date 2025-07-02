package com.surfing.inthe.wavepark.data.repository

import com.surfing.inthe.wavepark.data.api.WaterTemperatureApiService
import com.surfing.inthe.wavepark.data.model.TemperatureData
import com.surfing.inthe.wavepark.data.model.TemperatureResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureRepository @Inject constructor(
    private val waterTemperatureApiService: WaterTemperatureApiService
) {
    
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
            
            TemperatureResult.Success(temperatureData)
            
        } catch (e: Exception) {
            TemperatureResult.Error("수온 API 호출 실패: ${e.message}")
        }
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
} 