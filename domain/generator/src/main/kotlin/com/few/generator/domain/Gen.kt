package com.few.generator.domain

import com.few.generator.config.jpa.MutableListJsonConverter
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
data class Gen( // TODO: DB컬럼 타입 변경 필요
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(nullable = false) val provisioningContentsId: Long,
    @Convert(converter = MutableListJsonConverter::class)
    @Column(columnDefinition = "TEXT", nullable = false)
    val completionIds: MutableList<String> = mutableListOf(),
    @Column(columnDefinition = "TEXT", nullable = false) val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false) val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false) val highlightTexts: String = "[]",
    @Column(nullable = false) val category: Int,
    @Column(nullable = true) val region: Int,
) : BaseEntity()