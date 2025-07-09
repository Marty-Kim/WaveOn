package com.surfing.inthe.wavepark.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "temperature_data")
data class TemperatureDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val waterTemperature: Double,
    val airTemperature: Double,
    val location: String,
    val createdAt: Date = Date()
) 