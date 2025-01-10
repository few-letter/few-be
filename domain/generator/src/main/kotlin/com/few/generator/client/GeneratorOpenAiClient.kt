package com.few.generator.client

import com.few.generator.client.request.OpenAiRequest
import com.few.generator.client.response.OpenAiResponse
import com.few.generator.config.OpenAiFeignConfiguration
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "openAiClient",
    url = "\${openai.api.url}",
    configuration = [OpenAiFeignConfiguration::class],
)
interface GeneratorOpenAiClient {
    @PostMapping
    fun send(
        @RequestBody request: OpenAiRequest,
    ): OpenAiResponse
}