package com.surfing.inthe.wavepark.data.model

import java.util.Date

data class Event(
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
    val imageList: List<String> = listOf(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastSyncAt: Date = Date()
) 