package com.few.generator.service

import com.few.generator.domain.StockBriefingPostState
import com.few.generator.repository.StockBriefingPostStateRepository
import com.few.generator.support.jpa.GeneratorTransactional
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class StockBriefingPostStateService(
    private val repository: StockBriefingPostStateRepository,
) {
    private val atomicLastProcessedPostId = AtomicLong(UNINITIALIZED)

    companion object {
        private const val UNINITIALIZED = -1L
    }

    @GeneratorTransactional(readOnly = true)
    fun loadLastProcessedPostId(): Long? {
        val cached = atomicLastProcessedPostId.get()
        if (cached != UNINITIALIZED) return cached

        return repository
            .findById(StockBriefingPostState.SINGLETON_ID)
            .map { it.lastProcessedPostId }
            .orElse(null)
            ?.also { atomicLastProcessedPostId.compareAndSet(UNINITIALIZED, it) }
    }

    @GeneratorTransactional
    fun saveLastProcessedPostId(postId: Long) {
        repository.save(
            StockBriefingPostState(id = StockBriefingPostState.SINGLETON_ID, lastProcessedPostId = postId),
        )
        atomicLastProcessedPostId.set(postId)
    }
}