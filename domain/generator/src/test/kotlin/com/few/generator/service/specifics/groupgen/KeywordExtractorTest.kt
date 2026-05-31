package com.few.generator.service.specifics.groupgen

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.domain.Gen
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime

class KeywordExtractorTest :
    BehaviorSpec({
        val keyWordsCreator = mockk<KeyWordsCreator>()
        val extractor = KeywordExtractor(keyWordsCreator)

        fun makeGen(
            id: Long,
            coreTextsJson: String,
        ) = Gen(
            id = id,
            url = "https://example.com/article-$id",
            thumbnailImageUrl = null,
            mediaType = 1,
            headline = "헤드라인 $id",
            summary = "요약 $id",
            highlightTexts = "[]",
            coreTextsJson = coreTextsJson,
            category = Category.TECHNOLOGY.code,
            region = Region.LOCAL.code,
        ).apply { createdAt = LocalDateTime.now() }

        Given("Gen 목록이 있는 경우") {
            val coreTextsSlots = mutableListOf<String>()

            val gens =
                listOf(
                    makeGen(1L, """["AI 기술", "머신러닝"]"""),
                    makeGen(2L, """["반도체", "수출"]"""),
                )

            coEvery { keyWordsCreator.generateKeyWordsWithCoroutine(capture(coreTextsSlots)) } returnsMany
                listOf("AI,기술,머신러닝", "반도체,수출,무역")

            When("extractKeywordsFromGens를 호출하면") {
                val result = extractor.extractKeywordsFromGens(gens)

                Then("각 Gen의 coreTextsJson을 기반으로 키워드가 추출된다") {
                    result shouldHaveSize 2
                    coreTextsSlots[0] shouldBe """["AI 기술", "머신러닝"]"""
                    coreTextsSlots[1] shouldBe """["반도체", "수출"]"""
                }

                Then("GenDetail의 headline이 원본 Gen과 일치한다") {
                    result[0].headline shouldBe "헤드라인 1"
                    result[1].headline shouldBe "헤드라인 2"
                }
            }
        }

        Given("키워드 추출 중 일부 Gen에서 예외가 발생하는 경우") {
            val gens =
                listOf(
                    makeGen(1L, """["정상 데이터"]"""),
                    makeGen(2L, """["오류 데이터"]"""),
                )

            coEvery { keyWordsCreator.generateKeyWordsWithCoroutine("""["정상 데이터"]""") } returns "정상,키워드"
            coEvery { keyWordsCreator.generateKeyWordsWithCoroutine("""["오류 데이터"]""") } throws RuntimeException("GPT 오류")

            When("extractKeywordsFromGens를 호출하면") {
                val result = extractor.extractKeywordsFromGens(gens)

                Then("실패한 Gen은 제외되고 성공한 Gen만 반환된다") {
                    result shouldHaveSize 1
                    result[0].headline shouldBe "헤드라인 1"
                }
            }
        }

        Given("Gen 목록이 비어있는 경우") {
            When("extractKeywordsFromGens를 호출하면") {
                val result = extractor.extractKeywordsFromGens(emptyList())

                Then("빈 리스트가 반환된다") {
                    result.shouldBeEmpty()
                }
            }
        }
    })