package com.few.generator.client

import com.few.generator.config.GeneratorOpenAiFeignConfiguration
import com.few.generator.core.gpt.completion.ChatCompletion
import com.few.generator.core.gpt.prompt.Prompt
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "openAiClient",
    configuration = [GeneratorOpenAiFeignConfiguration::class],
)
interface GeneratorOpenAiClient {
    @PostMapping("\${openai.api.url}")
    fun send(
        @RequestBody request: Prompt,
    ): ChatCompletion
}