package com.few.generator.domain

import jakarta.persistence.*

@Entity
@Table(name = "gen")
data class Gen(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    // TODO: 필드 정의
) : BaseEntity()