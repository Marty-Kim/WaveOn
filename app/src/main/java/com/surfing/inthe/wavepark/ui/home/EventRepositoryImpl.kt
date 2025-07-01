package com.surfing.inthe.wavepark.ui.home

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    var events: List<EventItem>? = null

    override suspend fun getEventsFromFirestore(): List<EventItem> {
        println("Event snapshot :")
        if (!events.isNullOrEmpty()) {
            return events!!
        }
        return try {
            val snapshot = firestore.collection("events")
                .get()
                .await()
            println("Event snapshot : ${snapshot.size()}")

            events = snapshot.documents.mapNotNull { doc ->
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
            // TODO: 여기서 jsoup파싱으로 이벤트 상세의 이미지를 imageList에 담고 싶다


            events?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventImagesFromWeb(idx: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.wavepark.co.kr/board/event?act=view/detail/$idx"
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

    override suspend fun getEventsFromFirestoreWithImages(setEvents: (List<EventItem>) -> Unit): List<EventItem> {
        val snapshot = firestore.collection("events").get().await()
        val eventList = snapshot.documents.mapNotNull { doc ->
            val eventId = doc.getString("event_id") ?: return@mapNotNull null
            EventItem(
                imageUrl = doc.getString("image_url"),
                title = doc.getString("title") ?: "",
                event_url = doc.getString("event_url"),
                crawled_at = doc.getString("crawled_at"),
                d_day = (doc.get("d_day") as? Long)?.toInt() ?: 0,
                event_id = eventId,
                event_type = doc.getString("event_type"),
                date = doc.getString("date") ?: "",
                imageList = emptyList()
            )
        }
        // 먼저 기본 리스트를 콜백으로 전달
        setEvents(eventList)
        // 각 이벤트별로 jsoup 파싱을 비동기로 실행
        eventList.forEachIndexed { idx, event ->
            GlobalScope.launch(Dispatchers.IO) {
                val images = getEventImagesFromWeb(event.event_id ?: "")
                val updated = event.copy(imageList = images)
                // 리스트에서 해당 이벤트만 교체
                val newList = eventList.toMutableList().apply { set(idx, updated) }
                setEvents(newList)
            }
        }
        return eventList
    }
} 