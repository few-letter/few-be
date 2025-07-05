package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.vo.AsyncKeywordJob
import com.few.generator.domain.vo.GenDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class KeywordExtractionService(
    private val keyWordsService: KeyWordsService,
) {
    private val log = KotlinLogging.logger {}

    suspend fun extractKeywordsFromGens(
        gens: List<Gen>,
        provisioningContentsMap: Map<Long, ProvisioningContents>,
    ): List<GenDetail> {
        log.info { "키워드 추출 시작: ${gens.size}개 Gen 처리" }

        return coroutineScope {
            // 비동기로 키워드 추출 시작
            val keywordJobs =
                gens.map { gen ->
                    val coreTexts =
                        provisioningContentsMap[gen.provisioningContentsId]
                            ?.coreTextsJson ?: "키워드 없음"

                    AsyncKeywordJob(
                        gen = gen,
                        keywordDeferred = async { keyWordsService.generateKeyWordsWithCoroutine(coreTexts) },
                    )
                }

            // 모든 키워드 추출 완료 대기
            keywordJobs.map { job ->
                val keywords = job.keywordDeferred.await()
                log.debug { "Gen ${job.gen.id} 키워드 추출 완료: $keywords" }

                GenDetail(
                    headline = job.gen.headline,
                    keywords = keywords,
                )
            }
        }
    }
}