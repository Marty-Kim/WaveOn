package com.surfing.inthe.wavepark.data.repository

import com.surfing.inthe.wavepark.data.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEventsFlow(): Flow<List<Event>>
    suspend fun syncEventsIfNeeded()
    suspend fun fetchAndSaveEventImagesIfNeeded()
    suspend fun getEventById(eventId: String): Event?
} 