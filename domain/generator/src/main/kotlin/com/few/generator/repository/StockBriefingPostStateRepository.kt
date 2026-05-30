package com.few.generator.repository

import com.few.generator.domain.StockBriefingPostState
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StockBriefingPostStateRepository : JpaRepository<StockBriefingPostState, Long> {
    override fun findById(id: Long): Optional<StockBriefingPostState>

    override fun <S : StockBriefingPostState> save(entity: S): S
}