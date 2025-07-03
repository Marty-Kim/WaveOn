package com.surfing.inthe.wavepark.data.api

import retrofit2.http.GET

interface WaterTemperatureApiService {
    
    @GET("Prod/environment")
    suspend fun getWaterTemperature(): WaterTemperatureResponse
}

data class WaterTemperatureResponse(
    val temperature: Double,
    val humidity: Double,
    val water_temperature: Double,
    val timestamp: String
) 