package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.GptResponse
import com.few.generator.fixture.extensions.asString
import com.few.generator.fixture.gpt.KeywordsFixture
import com.few.generator.fixture.gpt.KeywordsTestData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class KeyWordsServiceTest :
    DescribeSpec({

        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val keyWordsService = KeyWordsService(promptGenerator, chatGpt)

        fun setupMocks(
            coreTexts: String,
            response: GptResponse,
        ) {
            val prompt = mockk<Prompt>()
            every { promptGenerator.toKoreanKeyWords(coreTexts) } returns prompt
            every { chatGpt.ask(prompt) } returns response
        }

        fun setupErrorMocks(
            coreTexts: String,
            exception: Exception,
        ) {
            val prompt = mockk<Prompt>()
            every { promptGenerator.toKoreanKeyWords(coreTexts) } returns prompt
            every { chatGpt.ask(prompt) } throws exception
        }

        describe("generateKeyWords") {
            it("핵심 텍스트에서 키워드를 생성한다") {
                val coreTexts = KeywordsTestData.DEFAULT_CORE_TEXT
                val keywords = KeywordsFixture.default().sample()
                setupMocks(coreTexts, keywords)

                val result = keyWordsService.generateKeyWords(coreTexts)

                result shouldBe keywords.asString()
                verify(exactly = 1) { promptGenerator.toKoreanKeyWords(coreTexts) }
                verify(exactly = 1) { chatGpt.ask(any()) }
            }

            it("빈 문자열에 대해서도 키워드를 생성한다") {
                val coreTexts = KeywordsTestData.EMPTY_TEXT
                val keywords = KeywordsFixture.empty().sample()
                setupMocks(coreTexts, keywords)

                val result = keyWordsService.generateKeyWords(coreTexts)

                result shouldBe keywords.asString()
            }

            it("GPT 응답이 Keywords 타입이 아니면 예외를 발생시킨다") {
                val coreTexts = KeywordsTestData.ERROR_TEXT
                setupMocks(coreTexts, mockk<GptResponse>()) // Not a Keywords instance

                shouldThrow<IllegalStateException> {
                    keyWordsService.generateKeyWords(coreTexts)
                }
            }
        }

        describe("generateKeyWordsAsync") {
            it("비동기적으로 키워드를 생성한다") {
                val coreTexts = KeywordsTestData.ASYNC_TEXT
                val keywords = KeywordsFixture.async().sample()
                setupMocks(coreTexts, keywords)

                val future = keyWordsService.generateKeyWordsAsync(coreTexts)
                val result = future.get(5, TimeUnit.SECONDS)

                result shouldBe keywords.asString()
            }

            it("비동기 처리 중 오류 발생 시 실패 메시지를 반환한다") {
                val coreTexts = KeywordsTestData.ASYNC_ERROR_TEXT
                setupErrorMocks(coreTexts, RuntimeException("API 오류"))

                val future = keyWordsService.generateKeyWordsAsync(coreTexts)
                val result = future.get(5, TimeUnit.SECONDS)

                result shouldBe KeywordsTestData.FAILURE_MESSAGE
            }
        }

        describe("generateKeyWordsWithCoroutine") {
            it("코루틴으로 키워드를 생성한다") {
                runBlocking {
                    val coreTexts = KeywordsTestData.COROUTINE_TEXT
                    val keywords = KeywordsFixture.coroutine().sample()
                    setupMocks(coreTexts, keywords)

                    val result = keyWordsService.generateKeyWordsWithCoroutine(coreTexts)

                    result shouldBe keywords.asString()
                }
            }

            it("코루틴 처리 중 오류 발생 시 실패 메시지를 반환한다") {
                runBlocking {
                    val coreTexts = KeywordsTestData.COROUTINE_ERROR_TEXT
                    setupErrorMocks(coreTexts, RuntimeException("API 오류"))

                    val result = keyWordsService.generateKeyWordsWithCoroutine(coreTexts)

                    result shouldBe KeywordsTestData.FAILURE_MESSAGE
                }
            }
        }
    })