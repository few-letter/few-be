package com.few.generator.domain

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.config.jpa.CategoryConverter
import com.few.generator.config.jpa.ContentsTypeConverter
import com.few.generator.config.jpa.MediaTypeConverter
import com.few.generator.config.jpa.RegionConverter
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
    @Column(columnDefinition = "TEXT", nullable = true) val url: String?,
    @Column(columnDefinition = "TEXT", nullable = true) val thumbnailImageUrl: String? = null,
    @Convert(converter = MediaTypeConverter::class)
    @Column(nullable = false) val mediaType: MediaType,
    @Column(columnDefinition = "TEXT", nullable = false) val headline: String,
    @Column(columnDefinition = "TEXT", nullable = false) val summary: String,
    @Column(columnDefinition = "TEXT", nullable = false) val highlightTexts: String = "[]",
    @Column(columnDefinition = "TEXT", nullable = false) val coreTextsJson: String = "[]",
    @Convert(converter = CategoryConverter::class)
    @Column(nullable = false) val category: Category,
    @Convert(converter = RegionConverter::class)
    @Column(nullable = true) val region: Region? = null,
    @Column(name = "published_via_skills_yn", columnDefinition = "CHAR(1)", nullable = true)
    val publishedViaSkillsYn: String? = null,
    @Convert(converter = ContentsTypeConverter::class)
    @Column(name = "contents_type", nullable = true)
    val contentsType: ContentsType? = null,
) : BaseEntity()