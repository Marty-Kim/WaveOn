package com.surfing.inthe.wavepark.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val startDate: Date,
    val endDate: Date?,
    val location: String?,
    val isActive: Boolean = true,
    val eventUrl: String? = null,
    val eventType: String? = null,
    val dDay: Int? = null,
    val imageList: String = "", // JSON 문자열로 저장
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastSyncAt: Date = Date() // 마지막 동기화 시간
) 