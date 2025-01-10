package com.few.generator.core

import com.few.generator.core.model.ContentSpec
import com.few.generator.core.model.GroupContentSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class Grouper(
    private val chatGpt: ChatGpt,
) {
    private val log = KotlinLogging.logger {}

    fun execute(contentSpecs: List<ContentSpec>): List<GroupContentSpec> {
        log.info { "Start Grouping ..." }
        val groupedContents =
            chatGpt.group(contentSpecs).apply {
                log.info { "Grouping completed." }
                log.info { "Complete grouping count: ${this.size()}" }
            }

        val result = mutableListOf<GroupContentSpec>()

        /**
         * Json 형식 [com.few.generator.core.PromptGenerator.createGroupingPrompt]
         */
        val groupElements = groupedContents.getAsJsonArray("groups")

        for (groupElement in groupElements) {
            val group = groupElement.asJsonObject
            val groupNewsIds = group.getAsJsonArray("content_ids").map { it.asString }
            val newsInGroup = contentSpecs.filter { it.id in groupNewsIds }
            if (newsInGroup.size >= 3) {
                val groupContentSpec =
                    GroupContentSpec(
                        topic = group.getAsJsonPrimitive("topic").asString,
                        contentSpecs = newsInGroup,
                    )
                result.add(groupContentSpec)
                log.info { "groupNewsIds: $groupNewsIds" }
            }
        }

        return result
    }
}