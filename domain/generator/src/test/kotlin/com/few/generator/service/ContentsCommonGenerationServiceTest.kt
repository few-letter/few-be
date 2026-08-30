package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.MediaType
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
import com.google.gson.Gson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class ContentsCommonGenerationServiceTest :
    BehaviorSpec({
        val rawContentsService = mockk<RawContentsService>()
        val provisioningService = mockk<ProvisioningService>()
        val genService = mockk<GenService>()
        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val gson = Gson()

        val service =
            ContentsCommonGenerationService(
                rawContentsService,
                provisioningService,
                genService,
                promptGenerator,
                chatGpt,
                gson,
            )

        fun rawContents(region: Region) =
            RawContents(
                url = "https://n.news.naver.com/article/001/12345",
                title = "테스트 기사",
                thumbnailImageUrl = "https://example.com/thumb.jpg",
                rawTexts = "본문 내용",
                category = Category.TECHNOLOGY.code,
                mediaType = 1,
                region = region.code,
            )

        fun provisioningContents(region: Region) =
            ProvisioningContents(
                coreTextsJson = """["핵심 문장1", "핵심 문장2"]""",
                category = Category.TECHNOLOGY.code,
                region = region.code,
            )

        val headlinePrompt = mockk<Prompt>()
        val summaryPrompt = mockk<Prompt>()
        val highlightPrompt = mockk<Prompt>()
        val headline = Headline("AI 기술 발전").also { it.completionId = "h-id" }
        val summary = Summary("AI가 빠르게 발전하고 있습니다.").also { it.completionId = "s-id" }
        val highlights = HighlightTexts(listOf("AI", "기술")).also { it.completionId = "hl-id" }

        fun stubGpt(
            raw: RawContents,
            provisioning: ProvisioningContents,
        ) {
            every { promptGenerator.toHeadlineShort(raw.title, provisioning.coreTextsJson) } returns headlinePrompt
            every {
                promptGenerator.toSummaryShort(headline.headline, raw.title, provisioning.coreTextsJson)
            } returns summaryPrompt
            every { promptGenerator.toKoreanHighlightText(summary.summary) } returns highlightPrompt
            every { chatGpt.ask(headlinePrompt) } returns headline
            every { chatGpt.ask(summaryPrompt) } returns summary
            every { chatGpt.ask(highlightPrompt) } returns highlights
        }

        Given("국내(LOCAL) 콘텐츠를 생성하는 경우") {
            val raw = rawContents(Region.LOCAL)
            val provisioning = provisioningContents(Region.LOCAL)

            every { rawContentsService.create(raw.url, Category.TECHNOLOGY, Region.LOCAL) } returns raw
            every { provisioningService.create(raw) } returns provisioning
            stubGpt(raw, provisioning)

            val genSlot = slot<Gen>()
            every { genService.saveWithNewTx(capture(genSlot)) } answers { genSlot.captured }

            When("createSingleContents를 호출하면") {
                service.createSingleContents(raw.url, Category.TECHNOLOGY, Region.LOCAL)
                val savedGen = genSlot.captured

                Then("RawContents의 url, thumbnailImageUrl, mediaType이 Gen에 포함된다") {
                    savedGen.url shouldBe raw.url
                    savedGen.thumbnailImageUrl shouldBe raw.thumbnailImageUrl
                    savedGen.mediaType shouldBe MediaType.from(raw.mediaType)
                }

                Then("ProvisioningContents의 coreTextsJson이 Gen에 포함된다") {
                    savedGen.coreTextsJson shouldBe provisioning.coreTextsJson
                }

                Then("GPT 결과가 Gen에 포함된다") {
                    savedGen.headline shouldBe headline.headline
                    savedGen.summary shouldBe summary.summary
                    savedGen.category shouldBe Category.TECHNOLOGY
                    savedGen.region shouldBe Region.LOCAL
                }

                Then("contentsType이 LOCAL_NEWS로 저장된다") {
                    savedGen.contentsType shouldBe ContentsType.LOCAL_NEWS
                }
            }
        }

        Given("해외(GLOBAL) 콘텐츠를 생성하는 경우") {
            val raw = rawContents(Region.GLOBAL)
            val provisioning = provisioningContents(Region.GLOBAL)

            every { rawContentsService.create(raw.url, Category.TECHNOLOGY, Region.GLOBAL) } returns raw
            every { provisioningService.create(raw) } returns provisioning
            stubGpt(raw, provisioning)

            val genSlot = slot<Gen>()
            every { genService.saveWithNewTx(capture(genSlot)) } answers { genSlot.captured }

            When("createSingleContents를 호출하면") {
                service.createSingleContents(raw.url, Category.TECHNOLOGY, Region.GLOBAL)
                val savedGen = genSlot.captured

                Then("contentsType이 GLOBAL_NEWS로 저장된다") {
                    savedGen.contentsType shouldBe ContentsType.GLOBAL_NEWS
                    savedGen.region shouldBe Region.GLOBAL
                }
            }
        }
    })