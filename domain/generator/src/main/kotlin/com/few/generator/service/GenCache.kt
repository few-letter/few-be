package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository

class GenCache(
    private val genRepository: GenRepository,
    private val dateRange: DateRange,
) {
    private val cache = mutableMapOf<Int, List<Gen>>()

    fun getGensByCategories(categories: List<Int>): List<Gen> =
        categories.flatMap { category ->
            cache.getOrPut(category) {
                genRepository.findAllByCreatedAtBetweenAndCategory(
                    dateRange.start,
                    dateRange.end,
                    category,
                )
            }
        }

    fun getAllGens(): List<Gen> = cache.values.flatten()
}