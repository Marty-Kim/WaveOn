package com.surfing.inthe.wavepark.shared.di

import com.surfing.inthe.wavepark.shared.api.WeatherApiService
import com.surfing.inthe.wavepark.shared.repository.WeatherRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object SharedModule {
    
    fun provideHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    
    fun provideWeatherApiService(httpClient: HttpClient): WeatherApiService {
        return WeatherApiService(httpClient)
    }
    
    fun provideWeatherRepository(weatherApiService: WeatherApiService): WeatherRepository {
        return WeatherRepository(weatherApiService)
    }
} 