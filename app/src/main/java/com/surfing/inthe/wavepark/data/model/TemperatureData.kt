package com.surfing.inthe.wavepark.data.model

data class TemperatureData(
    val temperature: Double?, // 기온
    val humidity: Double?, // 습도
    val waterTemperature: Double?, // 수온
    val weather: String?, // 날씨
    val recommendedWax: String?, // 수온에 따른 왁스 추천
    val timestamp: String? // 타임스탬프
)

sealed class TemperatureResult {
    data class Success(val data: TemperatureData) : TemperatureResult()
    data class Error(val message: String) : TemperatureResult()
} 