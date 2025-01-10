package com.few.crm.email.repository

import com.few.crm.email.domain.ScheduledEvent
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface ScheduledEventRepository : JpaRepository<ScheduledEvent, Long> {
    fun findByEventId(eventId: String): ScheduledEvent?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ScheduledEvent e WHERE e.eventId = :eventId AND e.completed = false")
    fun findByEventIdAndCompletedFalseForUpdate(eventId: String): ScheduledEvent?

    fun findAllByCompletedFalse(): List<ScheduledEvent>

    fun findAllByEventClassAndCompletedFalse(eventClass: String): List<ScheduledEvent>

    fun findAllByEventIdIn(eventIds: List<String>): List<ScheduledEvent>
}