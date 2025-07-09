package com.surfing.inthe.wavepark.data.database.dao

import androidx.room.*
import com.surfing.inthe.wavepark.data.database.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReservationDao {
    
    @Query("SELECT * FROM reservations ORDER BY sessionDate ASC")
    fun getAllReservations(): Flow<List<ReservationEntity>>
    
    @Query("SELECT * FROM reservations WHERE sessionDate >= :today ORDER BY sessionDate ASC")
    fun getUpcomingReservations(today: Date): Flow<List<ReservationEntity>>
    
    @Query("SELECT * FROM reservations WHERE reservationNumber = :reservationNumber")
    suspend fun getReservationByNumber(reservationNumber: String): ReservationEntity?
    
    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getReservationById(id: Long): ReservationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservations(reservations: List<ReservationEntity>)
    
    @Update
    suspend fun updateReservation(reservation: ReservationEntity)
    
    @Delete
    suspend fun deleteReservation(reservation: ReservationEntity)
    
    @Query("DELETE FROM reservations WHERE reservationNumber = :reservationNumber")
    suspend fun deleteReservationByNumber(reservationNumber: String)
    
    @Query("DELETE FROM reservations")
    suspend fun deleteAllReservations()
    
    @Query("SELECT COUNT(*) FROM reservations WHERE sessionDate >= :today")
    suspend fun getUpcomingReservationCount(today: Date): Int
} 