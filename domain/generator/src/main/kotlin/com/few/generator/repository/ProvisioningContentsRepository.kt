package com.few.generator.repository

import com.few.generator.domain.ProvisioningContents
import org.springframework.data.jpa.repository.JpaRepository

interface ProvisioningContentsRepository : JpaRepository<ProvisioningContents, Long>