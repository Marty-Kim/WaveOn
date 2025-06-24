package com.surfing.inthe.wavepark.data.api

import retrofit2.http.GET
import retrofit2.http.Path

data class ReservationData(
    val 시간: String,
    val 세션: String,
    val 방향: String,
    val 남은좌석: Int
)

data class ReservationResponse(
    val date: String,
    val data: List<ReservationData>
)

interface ReservationApiService {
    @GET("reservation/{date}")
    suspend fun getReservations(@Path("date") date: String): ReservationResponse
} 