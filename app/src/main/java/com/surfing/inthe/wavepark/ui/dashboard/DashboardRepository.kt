package com.surfing.inthe.wavepark.ui.dashboard

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * MVVM의 Repository 역할 (Dashboard 화면)
 * 데이터 소스(API, DB 등)와 ViewModel 사이의 추상화 계층.
 */

// Firestore daily_sessions 데이터 모델
data class DailySession(
    val isfunding: Boolean = false,
    val islesson: Boolean = false,
    val isNight: Boolean = false,
    val left: Int = 0,
    val name: String = "",
    val right: Int = 0,
    val time: String = "",
    val waves: String = ""
)

interface DashboardRepository {
   
    suspend fun getFutureDailySessions(limitDays: Int = 21): Map<String, List<DailySession>>
}

/**
 * 실제 데이터 제공 구현체. (API 연동)
 * @Inject 생성자: Hilt가 DI로 주입할 수 있게 함.-+
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
      private val firestore: FirebaseFirestore
  ) : DashboardRepository {
    

    override suspend fun getFutureDailySessions(limitDays: Int): Map<String, List<DailySession>> {
        return try {
            val now = java.time.LocalDate.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val end = now.plusDays((limitDays - 1).toLong())
            val snapshot = firestore.collection("daily_sessions").get().await()
            snapshot.documents
                .mapNotNull { doc ->
                    val date = doc.id
                    val docDate = try { java.time.LocalDate.parse(date, formatter) } catch (e: Exception) { null }
                    if (docDate != null && !docDate.isBefore(now) && !docDate.isAfter(end)) {
                        val sessions = doc.get("sessions") as? List<Map<String, Any>>
                        val sessionList = sessions?.map {
                            Log.d("LEFT LOG ","it  ${it["left"]}")
                            DailySession(
                                isfunding = it["isfunding"] as? Boolean ?: false,
                                isNight = it["isNight"] as? Boolean ?: false,
                                islesson = it["islesson"] as? Boolean ?: false,
                                left = (it["left"] as? Long)?.toInt() ?: 0,
                                name = it["name"] as? String ?: "",
                                right = (it["right"] as? Long)?.toInt() ?: 0,
                                time = it["time"] as? String ?: "",
                                waves = it["waves"] as? String ?: ""
                            )
                        } ?: emptyList()
                        date to sessionList
                    } else null
                }
                .toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
} 

