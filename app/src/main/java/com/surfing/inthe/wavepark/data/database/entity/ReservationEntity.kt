package com.surfing.inthe.wavepark.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reservationNumber: String,
    val sessionDate: Date,
    val sessionTime: String,
    val sessionType: String,
    val remainingSeats: Int,
    val totalSeats: Int,
    val price: Int,
    val status: String, // "confirmed", "cancelled", "pending"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 