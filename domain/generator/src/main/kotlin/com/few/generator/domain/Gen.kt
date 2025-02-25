package com.few.generator.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "gen")
@EntityListeners(AuditingEntityListener::class)
data class Gen(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    // TODO: 필드 정의
    @CreatedDate
    var createdAt: LocalDateTime? = null,
)