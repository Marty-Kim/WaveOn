package com.surfing.inthe.wavepark.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val weatherStatus: String,
    val temper: String,
    val baseDate: String,
    val baseTime: String,
    val fcstTime: String
)

@Serializable
sealed class WeatherResult {
    @Serializable
    data class Success(val data: WeatherData) : WeatherResult()
    @Serializable
    data class Error(val message: String) : WeatherResult()
} 