package com.few.generator.domain

import jakarta.persistence.*

@Entity
@Table(name = "provisioning_contents")
data class ProvisioningContents(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(columnDefinition = "TEXT", nullable = false)
    val bodyTextsJson: String = "[]", // JSON 문자열로 저장
    @Column(columnDefinition = "TEXT", nullable = false)
    val coreTextsJson: String = "[]", // JSON 문자열로 저장
) : BaseEntity() // TODO: completion의 ID 저장