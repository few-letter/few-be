package com.few.generator.repository

import com.few.generator.domain.ContentSource
import org.springframework.data.jpa.repository.JpaRepository

interface ContentSourceRepository : JpaRepository<ContentSource, Long>