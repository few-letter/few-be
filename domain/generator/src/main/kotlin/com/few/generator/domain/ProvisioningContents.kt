package com.few.generator.domain

import com.few.generator.config.jpa.MutableListJsonConverter
import jakarta.persistence.*

@Entity
@Table(
    name = "provisioning_contents",
    indexes = [Index(name = "idx_provisioning_contents_1", columnList = "raw_contents_id", unique = true)],
)
data class ProvisioningContents( // TODO: DB컬럼 타입 변경 필요
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val rawContentsId: Long,
    @Convert(converter = MutableListJsonConverter::class)
    @Column(columnDefinition = "TEXT", nullable = false)
    val completionIds: MutableList<String> = mutableListOf(),
    @Column(columnDefinition = "TEXT", nullable = false)
    val bodyTextsJson: String = "[]", // JSON 문자열로 저장
    @Column(columnDefinition = "TEXT", nullable = false)
    val coreTextsJson: String = "[]", // JSON 문자열로 저장
    @Column(nullable = false)
    val category: Int,
) : BaseEntity() {
    protected constructor() : this( // TODO: 기본 생성자 필요?
        id = null,
        rawContentsId = 0L,
        bodyTextsJson = "[]",
        coreTextsJson = "[]",
        completionIds = mutableListOf(),
        category = 0,
    )
}