package com.few.generator.domain

import com.few.generator.config.jpa.MutableListJsonConverter
import jakarta.persistence.*

@Entity
@Table(name = "gen")
data class Gen(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Convert(converter = MutableListJsonConverter::class)
    @Column(columnDefinition = "TEXT", nullable = false) // TODO: 타입 변경 필요
    val completionIds: MutableList<String> = mutableListOf(),
    @Column(columnDefinition = "TEXT", nullable = false) // TODO: 타입 변경 필요
    val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false) // TODO: 타입 변경 필요
    val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false) // TODO: 타입 변경 필요
    val highlightTexts: String = "[]",
) : BaseEntity()