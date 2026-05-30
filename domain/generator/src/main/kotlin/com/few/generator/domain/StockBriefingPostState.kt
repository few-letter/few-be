package com.few.generator.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "stock_briefing_post_state")
data class StockBriefingPostState(
    @Id val id: Long = SINGLETON_ID,
    @Column(nullable = false) val lastProcessedPostId: Long,
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}