package com.surfing.inthe.wavepark.ui.home

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {
    override suspend fun getEventsFromFirestore(): List<EventItem> {
        return try {
            val snapshot = firestore.collection("event")
                .orderBy("date")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val date = doc.getString("date") ?: ""
                val imageUrl = doc.getString("imageUrl")
                EventItem(
                    imageUrl = imageUrl,
                    title = title,
                    date = date
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 