package com.few.generator.core

import com.few.generator.core.model.GroupContentSpec
import com.few.generator.core.model.SectionContent
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class Summarizer(
    private val chatGpt: ChatGpt,
    private val mapper: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun execute(contentSpecs: List<GroupContentSpec>): List<GroupContentSpec> {
        for ((index, group) in contentSpecs.withIndex()) {
            log.info { "Processing group ${index + 1} / ${contentSpecs.size}" }
            chatGpt.summarizeGroup(group).apply {
                group.section = parseSection(this)
            }
            chatGpt.refineSummarizedGroup(group).apply {
                group.section = parseSection(this)
            }
        }

        return contentSpecs
    }

    private fun parseSection(data: JsonObject): SectionContent {
        // "section" 키에 해당하는 JSON 객체를 SectionContentModel로 역직렬화
        val sectionData = data.getAsJsonObject("section")
        return mapper.fromJson(sectionData, SectionContent::class.java)
    }
}