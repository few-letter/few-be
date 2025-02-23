package com.few.generator.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "raw_contents")
@EntityListeners(AuditingEntityListener::class)
data class RawContents(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val url: String,
    @Column(nullable = true)
    val title: String,
    @Column(nullable = true, length = 1000)
    val description: String,
    @Column(nullable = true)
    val thumbnailImageUrl: String? = null,
    @Column(nullable = true, columnDefinition = "TEXT")
    val rawTexts: String,
    @Column(nullable = true, columnDefinition = "TEXT")
    val imageUrls: String = "[]",
    @CreatedDate
    var createdAt: LocalDateTime? = null,
)