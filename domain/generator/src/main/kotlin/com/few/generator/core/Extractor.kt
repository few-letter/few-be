package com.few.generator.core

import com.few.generator.core.model.ContentSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component

@Component
class Extractor(
    private val chatGpt: ChatGpt,
) {
    private val log = KotlinLogging.logger {}

    suspend fun execute(contentSpecs: List<ContentSpec>): List<ContentSpec> {
        val semaphore = Semaphore(5) // 최대 동시 실행 개수 제한
        val routines = mutableListOf<Deferred<Unit>>()

        for (contentSpec in contentSpecs) {
            val routine =
                CoroutineScope(Dispatchers.IO).async {
                    semaphore.withPermit {
                        try {
                            val summarizedContents = chatGpt.summarize(contentSpec)
                            contentSpec.summary = summarizedContents.get("summary")?.asString ?: "Could not summarize"
                            contentSpec.importantSentences =
                                if (summarizedContents.has("important_sentences")) {
                                    val sentencesJsonArray = summarizedContents.getAsJsonArray("important_sentences")
                                    sentencesJsonArray.mapNotNull { it.asString }
                                } else {
                                    emptyList()
                                }
                        } catch (e: Exception) {
                            log.error { "Error occurred while summarizing ${contentSpec.title}: ${e.message}" }
                        }
                    }
                }
            routines.add(routine)
        }

        for ((index, routine) in routines.withIndex()) {
            routine.await()
            log.info { "Completed summarizing ${index + 1}/${contentSpecs.size}" }
        }

        return contentSpecs
    }
}