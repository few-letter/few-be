package com.few.generator.repository

import com.few.generator.domain.Gen
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenRepository : JpaRepository<Gen, Long>