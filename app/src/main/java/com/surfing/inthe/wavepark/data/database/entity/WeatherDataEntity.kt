package com.surfing.inthe.wavepark.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "weather_data")
data class WeatherDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: String,
    val weatherCondition: String,
    val icon: String,
    val createdAt: Date = Date()
) 