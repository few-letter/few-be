package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Texts
import com.few.generator.domain.vo.RawContents
import com.google.gson.Gson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

class ProvisioningServiceTest :
    BehaviorSpec({
        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val gson = Gson()

        val service = ProvisioningService(promptGenerator, chatGpt, gson)

        val rawContents =
            RawContents(
                url = "https://n.news.naver.com/article/001/12345",
                title = "테스트 기사",
                thumbnailImageUrl = null,
                rawTexts = "본문 내용입니다.",
                category = 1,
                mediaType = 0,
                region = 0,
            )

        Given("GPT 호출이 정상적인 경우") {
            val bodyPrompt = mockk<Prompt>()
            val corePrompt = mockk<Prompt>()
            val bodyTexts = Texts(listOf("문장1", "문장2")).also { it.completionId = "body-completion-id" }
            val coreTexts = Texts(listOf("핵심1")).also { it.completionId = "core-completion-id" }

            every { promptGenerator.toBodyTexts(rawContents.title, rawContents.rawTexts) } returns bodyPrompt
            every { promptGenerator.toCoreTexts(rawContents.title, bodyTexts) } returns corePrompt
            every { chatGpt.ask(bodyPrompt) } returns bodyTexts
            every { chatGpt.ask(corePrompt) } returns coreTexts

            When("create를 호출하면") {
                val result = service.create(rawContents)

                Then("coreTextsJson이 포함된 ProvisioningContents VO가 반환된다") {
                    result.coreTextsJson shouldNotBe "[]"
                    result.coreTextsJson shouldBe gson.toJson(coreTexts.texts)
                    result.category shouldBe rawContents.category
                    result.region shouldBe rawContents.region
                }
            }
        }
    })