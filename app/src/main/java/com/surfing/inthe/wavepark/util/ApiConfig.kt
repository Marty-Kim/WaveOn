package com.surfing.inthe.wavepark.util

import com.surfing.inthe.wavepark.BuildConfig

/**
 * API 설정을 관리하는 유틸리티 클래스
 * BuildConfig를 통해 안전하게 API 키와 URL을 관리합니다.
 */
object ApiConfig {
    
    /**
     * OpenWeather API 키
     */
    val OPENWEATHER_API_KEY: String = BuildConfig.OPENWEATHER_API_KEY
    
    /**
     * WavePark API 키
     */
    val WAVEPARK_API_KEY: String = BuildConfig.WAVEPARK_API_KEY
    
    /**
     * Firebase API 키
     */
    val FIREBASE_API_KEY: String = BuildConfig.FIREBASE_API_KEY
    
    /**
     * WavePark 기본 URL
     */
    val WAVEPARK_BASE_URL: String = BuildConfig.WAVEPARK_BASE_URL
    
    /**
     * 날씨 API 기본 URL
     */
    val WEATHER_API_BASE_URL: String = BuildConfig.WEATHER_API_BASE_URL
    
    /**
     * 수온 API 기본 URL
     */
    val TEMPERATURE_API_BASE_URL: String = BuildConfig.TEMPERATURE_API_BASE_URL
    
    /**
     * API 키가 유효한지 확인
     */
    fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && apiKey != "YOUR_OPENWEATHER_API_KEY" && 
               apiKey != "YOUR_WAVEPARK_API_KEY" && apiKey != "YOUR_FIREBASE_API_KEY"
    }
    
    /**
     * 모든 API 키가 설정되었는지 확인
     */
    fun areAllApiKeysConfigured(): Boolean {
        return isApiKeyValid(OPENWEATHER_API_KEY) && 
               isApiKeyValid(WAVEPARK_API_KEY) && 
               isApiKeyValid(FIREBASE_API_KEY)
    }
} 