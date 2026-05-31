package com.few.generator.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "gen",
    indexes =
        [
            Index(name = "idx_gen_created_at", columnList = "created_at DESC", unique = false),
            Index(name = "idx_gen_created_at_category", columnList = "created_at DESC, category", unique = false),
            Index(name = "idx_gen_category", columnList = "category", unique = false),
        ],
)
data class Gen(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(columnDefinition = "TEXT", nullable = false) val url: String,
    @Column(nullable = true) val thumbnailImageUrl: String? = null,
    @Column(nullable = false) val mediaType: Int,
    @Column(columnDefinition = "TEXT", nullable = false) val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false) val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false) val highlightTexts: String = "[]",
    @Column(columnDefinition = "TEXT", nullable = false) val coreTextsJson: String = "[]",
    @Column(nullable = false) val category: Int,
    @Column(nullable = true) val region: Int,
) : BaseEntity()