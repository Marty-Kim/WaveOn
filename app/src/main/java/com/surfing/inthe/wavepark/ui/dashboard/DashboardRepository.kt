package com.surfing.inthe.wavepark.ui.dashboard

import com.surfing.inthe.wavepark.data.api.ReservationApiService
import com.surfing.inthe.wavepark.data.api.ReservationData
import javax.inject.Inject

/**
 * MVVM의 Repository 역할 (Dashboard 화면)
 * 데이터 소스(API, DB 등)와 ViewModel 사이의 추상화 계층.
 */
interface DashboardRepository {
    suspend fun getReservationData(date: String): List<ReservationData>
}

/**
 * 실제 데이터 제공 구현체. (API 연동)
 * @Inject 생성자: Hilt가 DI로 주입할 수 있게 함.
 */
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ReservationApiService
) : DashboardRepository {
    override suspend fun getReservationData(date: String): List<ReservationData> {
        return apiService.getReservations(date).data
    }
} 