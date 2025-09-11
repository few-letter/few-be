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
    fun getRawContentsUrlsByGens(gens: List<Gen>): Map<Long, String> {
        if (gens.isEmpty()) {
            return emptyMap()
        }

        val provisioningIds = gens.map { it.provisioningContentsId }.distinct()
        if (provisioningIds.isEmpty()) return emptyMap()
        val provisioningContents = provisioningContentsRepository.findAllByIdIn(provisioningIds)

        val rawContentsIds = provisioningContents.map { it.rawContentsId }.distinct()
        if (rawContentsIds.isEmpty()) return emptyMap()
        val rawContents = rawContentsRepository.findAllByIdIn(rawContentsIds)

        val rawContentsMap: Map<Long, String> =
            rawContents.mapNotNull { rawContent -> rawContent.id?.let { it to rawContent.url } }.toMap()

        val provisioningMap =
            provisioningContents
                .mapNotNull { provisioning -> provisioning.id?.let { provisioning.id to provisioning } }
                .toMap()

        return gens
            .mapNotNull { gen ->
                val gId = gen.id ?: return@mapNotNull null
                val pId = gen.provisioningContentsId
                val rawId = provisioningMap[pId]?.rawContentsId ?: return@mapNotNull null
                rawContentsMap[rawId]?.let { url -> gId to url }
            }.toMap()
    }
}