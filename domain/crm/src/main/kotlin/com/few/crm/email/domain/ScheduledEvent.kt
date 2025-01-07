package com.few.crm.email.domain

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "scheduled_events")
@EntityListeners(AuditingEntityListener::class)
data class ScheduledEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "event_id")
    val eventId: String,
    @Column(name = "event_class")
    val eventClass: String,
    @Column(name = "event_payload")
    val eventPayload: String,
    @Column(name = "completed")
    var completed: Boolean,
    @Column(name = "is_not_consumed")
    var isNotConsumed: Boolean = false,
) {
    constructor() : this(
        eventId = "",
        eventClass = "",
        eventPayload = "",
        completed = false,
    )

    fun complete(): ScheduledEvent {
        completed = true
        return this
    }

    fun isNotConsumed(): ScheduledEvent {
        isNotConsumed = true
        return this
    }
}