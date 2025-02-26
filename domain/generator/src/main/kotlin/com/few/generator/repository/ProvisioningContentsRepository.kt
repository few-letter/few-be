package com.few.generator.repository

import com.few.generator.domain.ProvisioningContents
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProvisioningContentsRepository : JpaRepository<ProvisioningContents, Long>