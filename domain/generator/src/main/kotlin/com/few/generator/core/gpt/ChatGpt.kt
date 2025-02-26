package com.few.generator.core.gpt

import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.schema.GptResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "openai",
)
interface ChatGpt {
    @PostMapping
    fun ask(
        @RequestBody request: Prompt,
    ): GptResponse
}