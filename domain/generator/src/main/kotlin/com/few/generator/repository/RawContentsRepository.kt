package com.few.generator.repository

import com.few.generator.domain.RawContents
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RawContentsRepository : JpaRepository<RawContents, Long> {
    fun findByUrl(url: String): RawContents?
}