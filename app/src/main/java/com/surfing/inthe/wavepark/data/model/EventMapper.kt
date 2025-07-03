package com.surfing.inthe.wavepark.data.model

import com.surfing.inthe.wavepark.data.database.entity.EventEntity
import java.util.Date

object EventMapper {
    
    fun EventEntity.toEvent(): Event {
        return Event(
            eventId = this.eventId,
            title = this.title,
            description = this.description,
            imageUrl = this.imageUrl,
            startDate = this.startDate,
            endDate = this.endDate,
            location = this.location,
            isActive = this.isActive,
            eventUrl = this.eventUrl,
            eventType = this.eventType,
            dDay = this.dDay,
            imageList = if (this.imageList.isNotBlank()) this.imageList.split(",").map { it.trim() } else emptyList(),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastSyncAt = this.lastSyncAt
        )
    }
    
    fun Event.toEventEntity(): EventEntity {
        return EventEntity(
            eventId = this.eventId,
            title = this.title,
            description = this.description,
            imageUrl = this.imageUrl,
            startDate = this.startDate,
            endDate = this.endDate,
            location = this.location,
            isActive = this.isActive,
            eventUrl = this.eventUrl,
            eventType = this.eventType,
            dDay = this.dDay,
            imageList = this.imageList.joinToString(","),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastSyncAt = this.lastSyncAt
        )
    }
    
    fun List<EventEntity>.toEventList(): List<Event> {
        return this.map { it.toEvent() }
    }
    
    fun List<Event>.toEventEntityList(): List<EventEntity> {
        return this.map { it.toEventEntity() }
    }
} 