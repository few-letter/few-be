package com.few.crm.email.repository

import com.few.crm.email.domain.ScheduledEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduledEventRepository : JpaRepository<ScheduledEvent, Long> {
    fun findByEventId(eventId: String): ScheduledEvent?

    fun findAllByCompletedFalse(): List<ScheduledEvent>

    fun findAllByEventClassAndCompletedFalse(eventClass: String): List<ScheduledEvent>

    fun findAllByEventIdIn(eventIds: List<String>): List<ScheduledEvent>
}