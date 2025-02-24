package com.few.generator.core.gpt

import com.few.generator.core.gpt.completion.ChatCompletion
import com.few.generator.core.gpt.prompt.Prompt
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "openai",
    name = "openAiClient",
)
interface ChatGpt {
    @PostMapping
    fun ask(
        @RequestBody request: Prompt,
    ): ChatCompletion
}