package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import org.springframework.stereotype.Service

@Service
class GenUrlService(
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val rawContentsRepository: RawContentsRepository,
) {
    fun getUrlsByGens(gens: List<Gen>): Map<Long, String> {
        val provisioningIds = gens.map { it.provisioningContentsId }
        val provisioningContents = provisioningContentsRepository.findAllByIdIn(provisioningIds)

        val rawContentsIds = provisioningContents.map { it.rawContentsId }
        val rawContents = rawContentsRepository.findAllByIdIn(rawContentsIds)

        val rawContentsMap = rawContents.associateBy { it.id!! }
        val provisioningMap = provisioningContents.associateBy { it.id!! }

        return gens
            .mapNotNull { gen ->
                provisioningMap[gen.provisioningContentsId]?.let { provisioning ->
                    rawContentsMap[provisioning.rawContentsId]?.let { rawContent ->
                        gen.id!! to rawContent.url
                    }
                }
            }.toMap()
    }
}