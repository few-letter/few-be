package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.vo.ProvisioningContents
import com.few.generator.domain.vo.RawContents
import com.few.generator.repository.GenRepository
import com.google.gson.Gson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class GenServiceTest :
    BehaviorSpec({
        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val genRepository = mockk<GenRepository>()
        val gson = Gson()

        val service = GenService(promptGenerator, chatGpt, genRepository, gson)

        val rawContents =
            RawContents(
                url = "https://n.news.naver.com/article/001/12345",
                title = "테스트 기사",
                thumbnailImageUrl = "https://example.com/thumb.jpg",
                rawTexts = "본문 내용",
                category = Category.TECHNOLOGY.code,
                mediaType = 1,
                region = Region.LOCAL.code,
            )

        val provisioningContents =
            ProvisioningContents(
                coreTextsJson = """["핵심 문장1", "핵심 문장2"]""",
                category = Category.TECHNOLOGY.code,
                region = Region.LOCAL.code,
            )

        Given("GPT 호출이 모두 성공하는 경우") {
            val headlinePrompt = mockk<Prompt>()
            val summaryPrompt = mockk<Prompt>()
            val highlightPrompt = mockk<Prompt>()
            val headline = Headline("AI 기술 발전").also { it.completionId = "h-id" }
            val summary = Summary("AI가 빠르게 발전하고 있습니다.").also { it.completionId = "s-id" }
            val highlights = HighlightTexts(listOf("AI", "기술")).also { it.completionId = "hl-id" }

            every { promptGenerator.toHeadlineShort(rawContents.title, provisioningContents.coreTextsJson) } returns headlinePrompt
            every { promptGenerator.toSummaryShort(headline.headline, rawContents.title, provisioningContents.coreTextsJson) } returns
                summaryPrompt
            every { promptGenerator.toKoreanHighlightText(summary.summary) } returns highlightPrompt
            every { chatGpt.ask(headlinePrompt) } returns headline
            every { chatGpt.ask(summaryPrompt) } returns summary
            every { chatGpt.ask(highlightPrompt) } returns highlights

            val genSlot = slot<Gen>()
            every { genRepository.save(capture(genSlot)) } answers { genSlot.captured.also { it.let {} } }

            When("createAndSave를 호출하면") {
                service.createAndSave(rawContents, provisioningContents)
                val savedGen = genSlot.captured

                Then("RawContents의 url, thumbnailImageUrl, mediaType이 Gen에 포함된다") {
                    savedGen.url shouldBe rawContents.url
                    savedGen.thumbnailImageUrl shouldBe rawContents.thumbnailImageUrl
                    savedGen.mediaType shouldBe rawContents.mediaType
                }

                Then("ProvisioningContents의 coreTextsJson이 Gen에 포함된다") {
                    savedGen.coreTextsJson shouldBe provisioningContents.coreTextsJson
                }

                Then("GPT 결과가 Gen에 포함된다") {
                    savedGen.headline shouldBe headline.headline
                    savedGen.summary shouldBe summary.summary
                    savedGen.category shouldBe Category.TECHNOLOGY.code
                    savedGen.region shouldBe Region.LOCAL.code
                }
            }
        }
    })