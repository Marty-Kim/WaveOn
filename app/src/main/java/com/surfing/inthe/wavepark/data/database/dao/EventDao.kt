package com.surfing.inthe.wavepark.data.database.dao

import androidx.room.*
import com.surfing.inthe.wavepark.data.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {
    
    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY startDate DESC")
    fun getAllActiveEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE startDate >= :today AND isActive = 1 ORDER BY startDate ASC")
    fun getUpcomingEvents(today: Date): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventByLocalId(id: Long): EventEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: String)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
    
    @Query("SELECT COUNT(*) FROM events WHERE isActive = 1")
    suspend fun getActiveEventCount(): Int
} 