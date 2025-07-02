package com.surfing.inthe.wavepark.data.model

data class WeatherData(
    val weatherStatus: String,
    val temper: String,
    val baseDate: String,
    val baseTime: String,
    val fcstTime: String
)

sealed class WeatherResult {
    data class Success(val data: WeatherData) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
} 