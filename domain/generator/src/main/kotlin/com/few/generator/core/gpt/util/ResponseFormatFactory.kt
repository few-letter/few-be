package com.few.generator.core.gpt.util

import com.few.generator.core.gpt.prompt.JsonSchema
import com.few.generator.core.gpt.prompt.ResponseFormat
import com.few.generator.core.gpt.prompt.schema.GptResponse

object ResponseFormatFactory {
    inline fun <reified T : GptResponse> create(
        schemaName: String,
        schema: Map<String, Any>,
    ): ResponseFormat =
        ResponseFormat(
            jsonSchema = JsonSchema(schemaName, schema),
            responseClassType = T::class.java,
        )
}