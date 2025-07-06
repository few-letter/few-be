package com.few.generator.core.gpt.prompt

import com.google.gson.Gson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.string.shouldContain

// 커스텀 Matcher 정의
fun beAValidPrompt() =
    object : Matcher<Prompt> {
        override fun test(value: Prompt) =
            MatcherResult(
                value.messages.size == 2 &&
                    value.messages[0].role == ROLE.SYSTEM &&
                    value.messages[1].role == ROLE.USER,
                { "Prompt should have 2 messages (SYSTEM, USER)" },
                { "Prompt should not have 2 messages (SYSTEM, USER)" },
            )
    }

class PromptGeneratorTest :
    DescribeSpec({

        val gson = Gson()
        val promptGenerator = PromptGenerator(gson)

        describe("PromptGenerator") {

            describe("프롬프트 생성") {
                it("toKoreanKeyWords: 핵심 텍스트로 키워드 프롬프트를 생성한다") {
                    val coreTexts = "키워드 추출할 핵심 내용입니다."
                    val prompt = promptGenerator.toKoreanKeyWords(coreTexts)

                    prompt should beAValidPrompt()
                    prompt.messages[1].content shouldContain coreTexts
                    prompt.messages[1].content shouldContain "키워드"
                }

                it("toKoreanHighlightText: 요약으로 하이라이트 프롬프트를 생성한다") {
                    val summary = "하이라이트할 요약 내용입니다."
                    val prompt = promptGenerator.toKoreanHighlightText(summary)

                    prompt should beAValidPrompt()
                    prompt.messages[1].content shouldContain summary
                    prompt.messages[1].content shouldContain "하이라이트"
                }

                it("toGroupHeadlineOnlyPrompt: 헤드라인 목록으로 그룹 헤드라인 프롬프트를 생성한다") {
                    val headlines = listOf("헤드라인1", "헤드라인2", "헤드라인3")
                    val prompt = promptGenerator.toGroupHeadlineOnlyPrompt(headlines)

                    prompt should beAValidPrompt()
                    prompt.messages[1].content shouldContain "헤드라인1"
                    prompt.messages[1].content shouldContain "헤드라인2"
                    prompt.messages[1].content shouldContain "헤드라인3"
                }

                it("toGroupHighlightPrompt: 그룹 요약으로 하이라이트 프롬프트를 생성한다") {
                    val groupSummary = "그룹 요약 내용입니다."
                    val prompt = promptGenerator.toGroupHighlightPrompt(groupSummary)

                    prompt should beAValidPrompt()
                    prompt.messages[1].content shouldContain groupSummary
                    prompt.messages[1].content shouldContain "하이라이트"
                }
            }

            describe("프롬프트 구조 검증") {
                withData(
                    nameFn = { it.first },
                    "toKoreanKeyWords" to promptGenerator.toKoreanKeyWords("test"),
                    "toKoreanHighlightText" to promptGenerator.toKoreanHighlightText("test"),
                    "toGroupHeadlineOnlyPrompt" to promptGenerator.toGroupHeadlineOnlyPrompt(listOf("test")),
                    "toGroupHighlightPrompt" to promptGenerator.toGroupHighlightPrompt("test"),
                ) { (_, prompt) ->
                    prompt should beAValidPrompt()
                }
            }
        }
    })