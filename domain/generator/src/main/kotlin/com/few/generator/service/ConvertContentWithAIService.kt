package com.few.generator.service

import com.few.generator.core.Extractor
import com.few.generator.core.Grouper
import com.few.generator.core.Summarizer
import com.few.generator.core.model.ContentSpec
import com.few.generator.core.model.GroupContentSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class ConvertContentWithAIService(
    private val extractor: Extractor,
    private val grouper: Grouper,
    private val summarizer: Summarizer,
) {
    private val log = KotlinLogging.logger {}

    fun execute(contents: List<ContentSpec>): List<GroupContentSpec> =
        runBlocking {
            /**
             * 뉴스 추출 및 요약
             */
            val extractedContent =
                extractor.execute(contents).apply {
                    log.info { "Extracted ${this.size} news." }
                }

            /**
             *  뉴스 그룹화
             */
            val groupedContent =
                grouper.execute(extractedContent).apply {
                    log.info { "Grouped ${this.size} news." }
                }

            /**
             *  그룹 뉴스 요약
             */
            val summarizedGroups =
                summarizer.execute(groupedContent).apply {
                    log.info { "Summarized ${this.size} news." }
                }

            return@runBlocking summarizedGroups
        }
}