package com.few.generator.domain

import com.few.generator.support.jpa.converter.StringListConverter
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "crawl_urls")
@EntityListeners(AuditingEntityListener::class)
data class CrawlUrl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "urls", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var urls: List<String>,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
) {
    constructor() : this(
        urls = emptyList(),
    )
}