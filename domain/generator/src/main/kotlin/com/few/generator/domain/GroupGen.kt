package com.few.generator.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "group_gen",
    indexes = [
        Index(name = "idx_group_gen_1", columnList = "category", unique = false),
        Index(name = "idx_group_gen_2", columnList = "created_at DESC"),
    ],
)
data class GroupGen(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val category: Int,
    @Column(columnDefinition = "TEXT", nullable = false)
    val selectedGroupIds: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val highlightTexts: String = "[]",
    @Column(columnDefinition = "TEXT", nullable = false)
    val groupSourceHeadlines: String = "[]",
) : BaseEntity() {
    protected constructor() : this(
        id = null,
        category = 0,
        selectedGroupIds = "[]",
        headline = "",
        summary = "",
        highlightTexts = "[]",
        groupSourceHeadlines = "[]",
    )
}