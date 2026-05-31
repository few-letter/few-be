package com.few.generator.service.specifics.groupgen

import com.few.generator.domain.Gen
import com.few.generator.domain.vo.AsyncKeywordJob
import com.few.generator.domain.vo.GenDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service

@Service
class KeywordExtractor(
    private val keyWordsCreator: KeyWordsCreator,
) {
    private val log = KotlinLogging.logger {}

    suspend fun extractKeywordsFromGens(gens: List<Gen>): List<GenDetail> {
        log.info { "키워드 추출 시작: ${gens.size}개 Gen 처리" }

        return supervisorScope {
            val keywordJobs =
                gens.map { gen ->
                    AsyncKeywordJob(
                        gen = gen,
                        keywordDeferred = async { keyWordsCreator.generateKeyWordsWithCoroutine(gen.coreTextsJson) },
                    )
                }

            keywordJobs.mapNotNull { job ->
                try {
                    val keywords = job.keywordDeferred.await()
                    log.debug { "Gen ${job.gen.id} 키워드 추출 완료: $keywords" }
                    GenDetail(headline = job.gen.headline, keywords = keywords)
                } catch (e: Exception) {
                    log.warn(e) { "Gen ${job.gen.id} 키워드 추출 실패, 스킵합니다" }
                    null
                }
            }
        }
    }
}