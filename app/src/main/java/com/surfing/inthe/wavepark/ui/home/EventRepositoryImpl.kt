package com.surfing.inthe.wavepark.ui.home

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {
    override suspend fun getEventsFromFirestore(): List<EventItem> {
        println("Event snapshot :")
        return try {
            val snapshot = firestore.collection("events")
                .get()
                .await()
            println("Event snapshot : ${snapshot.size()}")

            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val event_url = doc.getString("event_url")
                val crawled_at = doc.getString("crawled_at")
                val d_day = (doc.get("d_day") as? Long)?.toInt() ?: 0
                val event_id = doc.getString("event_id")
                val event_type = doc.getString("event_type")
                val date = doc.getString("date") ?: ""
                val imageUrl = doc.getString("image_url")
                println("Event docs : ${title}")
                println("Event docs : ${imageUrl}")
                EventItem(
                    imageUrl = imageUrl,
                    title = title,
                    event_url = event_url,
                    crawled_at = crawled_at,
                    d_day = d_day,
                    event_id = event_id,
                    event_type = event_type,
                    date = date
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
} 