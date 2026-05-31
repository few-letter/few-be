package com.few.generator.service.specifics.groupgen

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.repository.GroupGenRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime

class GroupContentGeneratorTest :
    BehaviorSpec({
        val promptGenerator = mockk<PromptGenerator>()
        val chatGpt = mockk<ChatGpt>()
        val groupGenRepository = mockk<GroupGenRepository>()
        val gson = Gson()

        val generator = GroupContentGenerator(promptGenerator, chatGpt, groupGenRepository, gson)

        fun makeGen(
            id: Long,
            url: String,
            headline: String,
            summary: String,
        ) = Gen(
            id = id,
            url = url,
            thumbnailImageUrl = null,
            mediaType = 1,
            headline = headline,
            summary = summary,
            highlightTexts = "[]",
            coreTextsJson = "[]",
            category = Category.TECHNOLOGY.code,
            region = Region.LOCAL.code,
        ).apply { createdAt = LocalDateTime.now() }

        Given("GPT 호출이 모두 성공하는 경우") {
            val gens =
                listOf(
                    makeGen(1L, "https://example.com/article-1", "헤드라인1", "요약1"),
                    makeGen(2L, "https://example.com/article-2", "헤드라인2", "요약2"),
                    makeGen(3L, "https://example.com/article-3", "헤드라인3", "요약3"),
                )

            val group = Group(listOf(1, 2, 3))

            val headlinePrompt = mockk<Prompt>()
            val summaryPrompt = mockk<Prompt>()
            val highlightPrompt = mockk<Prompt>()
            val groupHeadline = Headline("그룹 헤드라인")
            val groupSummary = Summary("그룹 요약")
            val groupHighlights = HighlightTexts(listOf("키워드1", "키워드2"))

            every { promptGenerator.toGroupHeadlineOnlyPrompt(any()) } returns headlinePrompt
            every { promptGenerator.toGroupSummaryWithHeadlinesPrompt(any(), any(), any()) } returns summaryPrompt
            every { promptGenerator.toGroupHighlightPrompt(any()) } returns highlightPrompt
            every { chatGpt.ask(headlinePrompt) } returns groupHeadline
            every { chatGpt.ask(summaryPrompt) } returns groupSummary
            every { chatGpt.ask(highlightPrompt) } returns groupHighlights

            val groupGenSlot = slot<GroupGen>()
            every { groupGenRepository.save(capture(groupGenSlot)) } answers { groupGenSlot.captured }

            When("generateGroupContent를 호출하면") {
                generator.generateGroupContent(Category.TECHNOLOGY, gens, group, Region.LOCAL)
                val saved = groupGenSlot.captured

                Then("그룹 헤드라인과 요약이 저장된다") {
                    saved.headline shouldBe "그룹 헤드라인"
                    saved.summary shouldBe "그룹 요약"
                    saved.category shouldBe Category.TECHNOLOGY.code
                    saved.region shouldBe Region.LOCAL.code
                }

                Then("groupSourceHeadlines에 각 Gen의 url이 포함된다") {
                    val sourceHeadlines: List<GroupSourceHeadline> =
                        gson.fromJson(saved.groupSourceHeadlines, object : TypeToken<List<GroupSourceHeadline>>() {}.type)

                    sourceHeadlines shouldHaveSize 3
                    sourceHeadlines[0].url shouldBe "https://example.com/article-1"
                    sourceHeadlines[1].url shouldBe "https://example.com/article-2"
                    sourceHeadlines[2].url shouldBe "https://example.com/article-3"
                }
            }
        }
    })