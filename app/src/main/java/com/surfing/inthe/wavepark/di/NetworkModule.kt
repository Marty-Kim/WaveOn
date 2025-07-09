package com.surfing.inthe.wavepark.di

import com.google.gson.GsonBuilder
import com.surfing.inthe.wavepark.BuildConfig
import com.surfing.inthe.wavepark.data.api.WaterTemperatureApiService
import com.surfing.inthe.wavepark.data.api.WeatherApiService
import com.surfing.inthe.wavepark.util.ApiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(): Retrofit {
        var gson= GsonBuilder().setLenient().create()
        var loggingInterceptor = HttpLoggingInterceptor().apply {
            // DEBUG 모드에서만 로깅 활성화
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        var okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor).build()
        return Retrofit.Builder()
            .baseUrl(ApiConfig.WEATHER_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("weather") retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
    
    @Provides
    @Singleton
    @Named("temperature")
    fun provideWaterTemperatureRetrofit(): Retrofit {

        var gson= GsonBuilder().setLenient().create()
        var loggingInterceptor = HttpLoggingInterceptor().apply {
            // DEBUG 모드에서만 로깅 활성화
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        var okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor).build()
        return Retrofit.Builder()
            .baseUrl(ApiConfig.TEMPERATURE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWaterTemperatureApiService(@Named("temperature") retrofit: Retrofit): WaterTemperatureApiService {
        return retrofit.create(WaterTemperatureApiService::class.java)
    }


    @Provides
    @Singleton
    @Named("weather_api_key")
    fun provideWeatherApiKey(): String {
        return ApiConfig.OPENWEATHER_API_KEY
    }
    
    @Provides
    @Singleton
    @Named("wavepark_api_key")
    fun provideWaveparkApiKey(): String {
        return ApiConfig.WAVEPARK_API_KEY
    }
    
    @Provides
    @Singleton
    @Named("wavepark_base_url")
    fun provideWaveparkBaseUrl(): String {
        return ApiConfig.WAVEPARK_BASE_URL
    }
} 