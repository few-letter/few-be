package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightText
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.fixture.RandomDataGenerator
import com.few.generator.repository.GenRepository
import com.google.gson.Gson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GenServiceTest :
    DescribeSpec({

        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val genRepository = mockk<GenRepository>()
        val gson = Gson()
        val genService = GenService(promptGenerator, chatGpt, genRepository, gson)

        describe("GenService") {

            describe("create") {
                it("RawContents와 ProvisioningContents로 Gen을 생성한다") {
                    val rawContents = RandomDataGenerator.randomRawContents()
                    val provisioningContents = RandomDataGenerator.randomProvisioningContents()

                    val headlinePrompt = mockk<Prompt>()
                    val summaryPrompt = mockk<Prompt>()
                    val highlightPrompt = mockk<Prompt>()

                    val mockHeadline = Headline("생성된 헤드라인").apply { completionId = "completion-1" }
                    val mockSummary = Summary("생성된 요약").apply { completionId = "completion-2" }
                    val mockHighlightText = HighlightText("생성된 하이라이트").apply { completionId = "completion-3" }

                    val expectedGen =
                        Gen(
                            id = 1L,
                            provisioningContentsId = provisioningContents.id!!,
                            completionIds =
                                mutableListOf(
                                    mockHeadline.completionId!!,
                                    mockSummary.completionId!!,
                                    mockHighlightText.completionId!!,
                                ),
                            headline = mockHeadline.headline,
                            summary = mockSummary.summary,
                            highlightTexts = gson.toJson(listOf(mockHighlightText.highlightText)),
                            category = Category.from(provisioningContents.category).code,
                        )

                    every { promptGenerator.toHeadlineShort(any(), any(), any()) } returns headlinePrompt
                    every { promptGenerator.toSummaryShort(any(), any(), any(), any()) } returns summaryPrompt
                    every { promptGenerator.toKoreanHighlightText(any()) } returns highlightPrompt

                    every { chatGpt.ask(headlinePrompt) } returns mockHeadline
                    every { chatGpt.ask(summaryPrompt) } returns mockSummary
                    every { chatGpt.ask(highlightPrompt) } returns mockHighlightText

                    every { genRepository.save(any<Gen>()) } returns expectedGen

                    val result = genService.create(rawContents, provisioningContents)

                    result.headline shouldBe mockHeadline.headline
                    result.summary shouldBe mockSummary.summary
                    result.category shouldBe Category.from(provisioningContents.category).code

                    verify {
                        promptGenerator.toHeadlineShort(
                            rawContents.title,
                            rawContents.description,
                            provisioningContents.coreTextsJson,
                        )
                    }
                    verify {
                        promptGenerator.toSummaryShort(
                            mockHeadline.headline,
                            rawContents.title,
                            rawContents.description,
                            provisioningContents.coreTextsJson,
                        )
                    }
                    verify { promptGenerator.toKoreanHighlightText(mockSummary.summary) }
                    verify { chatGpt.ask(headlinePrompt) }
                    verify { chatGpt.ask(summaryPrompt) }
                    verify { chatGpt.ask(highlightPrompt) }
                    verify { genRepository.save(any<Gen>()) }
                }
            }
        }
    })