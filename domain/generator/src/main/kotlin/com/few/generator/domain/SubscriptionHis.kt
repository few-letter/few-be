package com.few.generator.domain

import com.few.common.domain.ContentsType
import com.few.generator.config.jpa.ContentsTypeConverter
import jakarta.persistence.*

@Entity
@Table(
    name = "subscription_history",
)
class SubscriptionHis(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column var email: String,
    @Column(name = "categories", length = 100) var categories: String,
    @Column var action: Int,
    @Convert(converter = ContentsTypeConverter::class)
    @Column(name = "contents_type", nullable = false)
    var contentsType: ContentsType,
) : BaseEntity()