package com.surfing.inthe.wavepark.ui.home

import com.surfing.inthe.wavepark.ui.home.EventItem
import javax.inject.Singleton

interface EventRepository {
    suspend fun getEventsFromFirestore(): List<EventItem>
    suspend fun getEventImagesFromWeb(idx: String): List<String>
    suspend fun getEventsFromFirestoreWithImages(setEvents: (List<EventItem>) -> Unit): List<EventItem>
}