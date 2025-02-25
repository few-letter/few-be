package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import org.springframework.stereotype.Service

@Service
class GenService(
    private val chatGpt: ChatGpt,
) {
    fun create(
        rawContents: RawContents,
        provisioningContents: ProvisioningContents,
    ): List<Gen> {
        TODO("Not yet implemented")
    }
}