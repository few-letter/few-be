package com.few.generator.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "instagram_token")
class InstagramToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(columnDefinition = "TEXT", nullable = false) val accessToken: String,
    @Column(nullable = false) val expiresIn: Long,
    @Column(nullable = false) val expiresAt: LocalDateTime,
) : BaseEntity()