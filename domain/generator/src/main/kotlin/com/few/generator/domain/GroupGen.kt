package com.few.generator.domain

import com.few.generator.config.jpa.GroupSourceHeadlinesConverter
import com.few.generator.domain.vo.GroupSourceHeadline
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
    @Column(nullable = false)
    val groupIndices: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val highlightTexts: String = "[]",
    @Convert(converter = GroupSourceHeadlinesConverter::class)
    @Column(columnDefinition = "TEXT", nullable = false)
    val groupSourceHeadlines: List<GroupSourceHeadline> = mutableListOf(),
) : BaseEntity() {
    constructor() : this(
        id = null,
        category = 0,
        groupIndices = "[]",
        headline = "",
        summary = "",
        highlightTexts = "[]",
        groupSourceHeadlines = mutableListOf(),
    )
}