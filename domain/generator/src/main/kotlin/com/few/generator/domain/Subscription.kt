package com.few.generator.domain

import com.few.common.domain.ContentsType
import com.few.generator.config.jpa.ContentsTypeConverter
import jakarta.persistence.*

@Entity
@Table(
    name = "subscription",
    indexes =
        [
            Index(name = "idx_subscription_email_contents_type", columnList = "email, contents_type"),
        ],
)
class Subscription(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(length = 100) var email: String,
    @Column(name = "categories", length = 100) var categories: String,
    @Convert(converter = ContentsTypeConverter::class)
    @Column(name = "contents_type", nullable = false)
    var contentsType: ContentsType,
) : BaseEntity()