package com.few.provider.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "subscription",
    indexes =
        [
            Index(
                name = "idx_subscription_email_category",
                columnList = "email, category_code",
            ),
            Index(name = "idx_subscription_email", columnList = "email"),
        ],
)
class Subscription(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(unique = true) var email: String,
    @Column var category: Int,
) : BaseEntity()