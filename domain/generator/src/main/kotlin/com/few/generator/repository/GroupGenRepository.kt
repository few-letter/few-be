package com.few.generator.repository

import com.few.generator.domain.GroupGen
import org.springframework.data.jpa.repository.JpaRepository

interface GroupGenRepository : JpaRepository<GroupGen, Long>