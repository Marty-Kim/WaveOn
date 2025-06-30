package com.surfing.inthe.wavepark.ui.home

import com.surfing.inthe.wavepark.ui.home.EventItem

interface EventRepository {
    suspend fun getEventsFromFirestore(): List<EventItem>
} 