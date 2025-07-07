package com.surfing.inthe.wavepark.shared.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Event(
    val eventId: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val startDate: Instant,
    val endDate: Instant? = null,
    val location: String? = null,
    val isActive: Boolean = true,
    val eventUrl: String? = null,
    val eventType: String? = null,
    val dDay: Int? = null,
    val imageList: List<String> = listOf(),
    val createdAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val updatedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val lastSyncAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
) 