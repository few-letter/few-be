package com.few.generator.repository

import com.few.generator.domain.RawContents
import org.springframework.data.jpa.repository.JpaRepository

interface RawContentsRepository : JpaRepository<RawContents, Long> {
    override fun <S : RawContents> saveAll(entities: Iterable<S>): List<S>

    fun findByUrl(url: String): RawContents?

    fun findAllByIdIn(ids: List<Long>): List<RawContents>
}