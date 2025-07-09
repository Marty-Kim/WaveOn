package com.surfing.inthe.wavepark.data.model

import java.util.Date

data class Reservation(
    val reservationNumber: String,
    val sessionDate: Date,
    val sessionTime: String,
    val sessionType: String,
    val remainingSeats: Int,
    val totalSeats: Int,
    val price: Int,
    val status: String // "confirmed", "cancelled", "pending"
) 