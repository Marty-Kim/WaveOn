package com.surfing.inthe.wavepark.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class TemperatureData(
    val temperature: Double? = null, // 기온
    val humidity: Double? = null, // 습도
    val waterTemperature: Double? = null, // 수온
    val weather: String? = null, // 날씨
    val recommendedWax: String? = null, // 수온에 따른 왁스 추천
    val timestamp: String? = null // 타임스탬프
)

@Serializable
sealed class TemperatureResult {
    @Serializable
    data class Success(val data: TemperatureData) : TemperatureResult()
    @Serializable
    data class Error(val message: String) : TemperatureResult()
} 