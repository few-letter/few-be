package com.few.generator.service

import com.few.generator.domain.ProvisioningContents
import com.few.generator.repository.ProvisioningContentsRepository
import org.springframework.stereotype.Service

@Service
class ProvisioningContentsService(
    private val provisioningContentsRepository: ProvisioningContentsRepository,
) {
    fun findAllByIdIn(ids: List<Long>): List<ProvisioningContents> = provisioningContentsRepository.findAllByIdIn(ids)
}