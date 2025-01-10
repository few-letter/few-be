package com.few.generator.domain

import com.few.generator.core.model.GroupContentSpec
import com.few.generator.support.jpa.converter.GroupContentSpecListConverter
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "content_sources")
@EntityListeners(AuditingEntityListener::class)
data class ContentSource(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Lob
    @Column(name = "source", columnDefinition = "MEDIUMTEXT")
    @Convert(converter = GroupContentSpecListConverter::class)
    var source: List<GroupContentSpec>,
    @Column(name = "crawl_url_id")
    var crawlUrlId: Long,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
) {
    constructor() : this(
        source = emptyList(),
        crawlUrlId = 0,
    )
}