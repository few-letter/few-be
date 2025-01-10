package com.few.generator.repository

import com.few.generator.domain.CrawlUrl
import org.springframework.data.jpa.repository.JpaRepository

interface CrawlUrlRepository : JpaRepository<CrawlUrl, Long>