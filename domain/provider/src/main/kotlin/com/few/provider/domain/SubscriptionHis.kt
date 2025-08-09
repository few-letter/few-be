package com.few.provider.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "subscription_history",
    indexes =
        [
            Index(name = "idx_subscription_his_email", columnList = "email"),
            Index(
                name = "idx_subscription_his_email_created",
                columnList = "email, created_at",
            ),
        ],
)
class SubscriptionHis(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column var email: String,
    @Column var category: Int,
    @Column var action: Int,
) : BaseEntity()