package com.few.generator.repository

import com.few.generator.domain.RawContents
import org.springframework.data.jpa.repository.JpaRepository

interface RawContentsRepository : JpaRepository<RawContents, Long> {
    fun findByUrl(url: String): RawContents?
}