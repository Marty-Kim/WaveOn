package com.surfing.inthe.wavepark.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.surfing.inthe.wavepark.data.database.dao.EventDao
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.data.model.EventMapper.toEvent
import com.surfing.inthe.wavepark.data.model.EventMapper.toEventEntityList
import com.surfing.inthe.wavepark.data.model.EventMapper.toEventList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val firestore: FirebaseFirestore
) : EventRepository {
    override fun getEventsFlow(): Flow<List<Event>> =
        eventDao.getAllActiveEvents().map { it.toEventList() }

    override suspend fun syncEventsIfNeeded() {
        val eventCount = eventDao.getTotalEventCount()
        val lastSyncTime = eventDao.getLastSyncTime()
        val currentTime = Date()
        if (eventCount == 0 || lastSyncTime == null || isDataStale(lastSyncTime, currentTime)) {
            val events = fetchEventsFromFirestore()
            eventDao.insertEvents(events.toEventEntityList())
        }
    }

    private suspend fun fetchEventsFromFirestore(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("events").get().await()
            snapshot.documents.mapNotNull { doc ->
                val eventId = doc.getString("event_id") ?: return@mapNotNull null
                val title = doc.getString("title") ?: return@mapNotNull null
                Event(
                    eventId = eventId,
                    title = title,
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("image_url"),
                    startDate = Date(),
                    endDate = null,
                    location = doc.getString("location"),
                    isActive = true,
                    eventUrl = doc.getString("event_url"),
                    eventType = doc.getString("event_type"),
                    dDay = (doc.get("d_day") as? Long)?.toInt(),
                    imageList = emptyList(),
                    lastSyncAt = Date()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun fetchAndSaveEventImagesIfNeeded() {
        val events = eventDao.getAllEvents().map { it.toEventList() }
        events.collect { eventList ->
            eventList.filter { it.imageList.isEmpty() && it.eventId.isNotEmpty() }.forEach { event ->
                val images = fetchImagesByJsoup(event.eventId)
                if (images.isNotEmpty()) {
                    val entity = eventDao.getEventById(event.eventId)
                    entity?.let {
                        val updated = it.copy(imageList = images.joinToString(","), updatedAt = Date())
                        eventDao.updateEvent(updated)
                    }
                }
            }
        }
    }

    private suspend fun fetchImagesByJsoup(eventId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.wavepark.co.kr/board/event?act=view/detail/$eventId"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get()
            val imageElements = doc.select("article div.con img")
            imageElements.map { img ->
                val src = img.attr("src")
                if (src.startsWith("http")) src else "https://www.wavepark.co.kr/$src"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventById(eventId: String): Event? =
        eventDao.getEventById(eventId)?.toEvent()

    private fun isDataStale(lastSyncTime: Date, currentTime: Date): Boolean {
        val diffInMillis = currentTime.time - lastSyncTime.time
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        return diffInHours >= 6
    }
} 