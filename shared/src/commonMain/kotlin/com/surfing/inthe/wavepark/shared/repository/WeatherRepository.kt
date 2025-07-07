package com.surfing.inthe.wavepark.shared.repository

import com.surfing.inthe.wavepark.shared.api.WeatherApiService
import com.surfing.inthe.wavepark.shared.model.WeatherData
import com.surfing.inthe.wavepark.shared.model.WeatherResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(private val weatherApiService: WeatherApiService) {
    
    fun getWeatherData(
        serviceKey: String,
        baseDate: String,
        baseTime: String
    ): Flow<WeatherResult> = flow {
        try {
            val response = weatherApiService.getUltraSrtFcst(
                serviceKey = serviceKey,
                baseDate = baseDate,
                baseTime = baseTime
            )
            
            // 응답 데이터를 WeatherData로 변환하는 로직
            val weatherData = parseWeatherResponse(response)
            emit(WeatherResult.Success(weatherData))
        } catch (e: Exception) {
            emit(WeatherResult.Error(e.message ?: "Unknown error"))
        }
    }
    
    private fun parseWeatherResponse(response: com.surfing.inthe.wavepark.shared.api.WeatherResponse): WeatherData {
        // 실제 파싱 로직 구현
        val items = response.response.body.items.item
        
        val temperature = items.find { it.category == "T1H" }?.fcstValue ?: ""
        val weatherStatus = items.find { it.category == "SKY" }?.fcstValue ?: ""
        
        return WeatherData(
            weatherStatus = weatherStatus,
            temper = temperature,
            baseDate = items.firstOrNull()?.baseDate ?: "",
            baseTime = items.firstOrNull()?.baseTime ?: "",
            fcstTime = items.firstOrNull()?.fcstTime ?: ""
        )
    }
} 