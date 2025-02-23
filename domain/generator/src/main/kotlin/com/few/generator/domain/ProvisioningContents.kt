package com.few.generator.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "provisioning_contents")
@EntityListeners(AuditingEntityListener::class)
data class ProvisioningContents(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(columnDefinition = "TEXT", nullable = false)
    val bodyTextsJson: String = "[]", // JSON 문자열로 저장
    @Column(columnDefinition = "TEXT", nullable = false)
    val coreTextsJson: String = "[]", // JSON 문자열로 저장
    @CreatedDate
    var createdAt: LocalDateTime? = null,
)