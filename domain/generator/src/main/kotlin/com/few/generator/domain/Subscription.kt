package com.few.generator.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "subscription",
    indexes =
        [
            Index(name = "idx_subscription_email", columnList = "email", unique = true),
        ],
)
class Subscription(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(unique = true, length = 100) var email: String,
    @Column(name = "categories", length = 100) var categories: String,
) : BaseEntity()