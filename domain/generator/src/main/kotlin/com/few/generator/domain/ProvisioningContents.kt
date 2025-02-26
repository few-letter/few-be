package com.few.generator.domain

import com.few.generator.config.jpa.MutableListJsonConverter
import jakarta.persistence.*

@Entity
@Table(name = "provisioning_contents")
data class ProvisioningContents(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Convert(converter = MutableListJsonConverter::class)
    @Column(columnDefinition = "TEXT", nullable = false) // TODO: 타입 변경 필요
    val completionIds: MutableList<String> = mutableListOf(),
    @Column(columnDefinition = "TEXT", nullable = false)
    val bodyTextsJson: String = "[]", // JSON 문자열로 저장
    @Column(columnDefinition = "TEXT", nullable = false)
    val coreTextsJson: String = "[]", // JSON 문자열로 저장
) : BaseEntity() // TODO: completion의 ID 저장