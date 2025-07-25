package com.few.generator.config.feign

import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.core.gpt.prompt.Prompt
import com.google.gson.Gson
import feign.RequestTemplate
import feign.codec.Encoder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Component
class OpenAiEncoder(
    @Qualifier(GeneratorGsonConfig.GSON_BEAN_NAME)
    private val gson: Gson,
) : Encoder {
    override fun encode(
        obj: Any?,
        bodyType: Type?,
        template: RequestTemplate?,
    ) {
        runCatching {
            ResponseClassThreadLocal.set((obj as Prompt).responseFormat.responseClassType)
            template?.body(gson.toJson(obj))
        }.onFailure {
            ResponseClassThreadLocal.clear()
            throw RuntimeException("Failed to encode request body", it)
        }
    }
}