package com.few.provider.domain

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
) : BaseEntity()