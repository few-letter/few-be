package com.few.generator.usecase

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.core.scrapper.naver.StockBriefingRawContent
import com.few.generator.event.StockBriefingContentProcessedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.service.StockBriefingPostStateService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher

class StockBriefingSchedulingUseCaseTest :
    BehaviorSpec({
        val scrapper = mockk<Scrapper>()
        val chatGpt = mockk<ChatGpt>()
        val promptGenerator = mockk<PromptGenerator>()
        val publisher = mockk<ApplicationEventPublisher>(relaxed = true)
        val stockBriefingPostStateService = mockk<StockBriefingPostStateService>()
        val fetchedPostId = 3361L

        val useCase =
            StockBriefingSchedulingUseCase(
                scrapper = scrapper,
                chatGpt = chatGpt,
                promptGenerator = promptGenerator,
                applicationEventPublisher = publisher,
                stockBriefingPostStateService = stockBriefingPostStateService,
            )

        val dummyPrompt = mockk<Prompt>()

        beforeEach {
            clearMocks(scrapper, chatGpt, publisher, stockBriefingPostStateService)
            every { scrapper.fetchStockBriefingLatestPostId(any()) } returns fetchedPostId
            every { stockBriefingPostStateService.saveLastProcessedPostId(any()) } just Runs
        }

        Given("목록 API에서 postId를 가져올 수 없는 경우") {
            When("execute를 호출하면") {
                Then("RuntimeException이 발생하고 이벤트가 발행되지 않는다") {
                    every { scrapper.fetchStockBriefingLatestPostId(any()) } returns null

                    shouldThrow<RuntimeException> { useCase.execute() }

                    verify(exactly = 0) { publisher.publishEvent(any()) }
                    verify(exactly = 0) { stockBriefingPostStateService.saveLastProcessedPostId(any()) }
                }
            }
        }

        Given("신규 포스트가 존재하고 크롤링/GPT 처리가 모두 성공하는 경우") {
            val rawContents =
                listOf(
                    StockBriefingRawContent("코스피 상승", "코스피가 2% 상승했다."),
                    StockBriefingRawContent("나스닥 강세", "나스닥 지수가 사상 최고치를 기록했다."),
                )

            beforeEach {
                every { scrapper.scrapeStockBriefingPost(fetchedPostId) } returns rawContents
                every { promptGenerator.toStockBriefingHeadline(any(), any()) } returns dummyPrompt
                every { promptGenerator.toStockBriefingSummary(any(), any(), any()) } returns dummyPrompt
                every { promptGenerator.toKoreanHighlightText(any()) } returns dummyPrompt
                every { promptGenerator.toStockBriefingMainPageBody(any()) } returns dummyPrompt
                every { chatGpt.ask(dummyPrompt) } returnsMany
                    listOf(
                        Headline("코스피 2% 급등"),
                        Summary("코스피가 강한 상승세로 2% 급등."),
                        HighlightTexts(listOf("코스피", "2% 급등")),
                        Headline("나스닥 사상 최고치 경신"),
                        Summary("나스닥이 사상 최고치를 돌파."),
                        HighlightTexts(listOf("나스닥", "사상 최고치")),
                        Summary("코스피와 나스닥이 동반 상승하며 강한 상승세를 보였습니다."),
                    )
            }

            When("execute를 호출하면") {
                Then("마지막 postId가 DB에 저장된다") {
                    useCase.execute()

                    verify { stockBriefingPostStateService.saveLastProcessedPostId(fetchedPostId) }
                }

                Then("StockBriefingContentProcessedEvent가 처리된 컨텐츠와 함께 발행된다") {
                    useCase.execute()

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingContentProcessedEvent> {
                                it.postId == fetchedPostId &&
                                    it.contents.size == 2 &&
                                    it.contents[0].headline == "코스피 2% 급등" &&
                                    it.contents[1].headline == "나스닥 사상 최고치 경신" &&
                                    it.headlines == listOf("코스피 2% 급등", "나스닥 사상 최고치 경신")
                            },
                        )
                    }
                }
            }
        }

        Given("크롤링 결과가 비어있는 경우") {
            When("execute를 호출하면") {
                Then("postId만 저장되고 이벤트는 발행되지 않는다") {
                    every { scrapper.scrapeStockBriefingPost(fetchedPostId) } returns emptyList()

                    useCase.execute()

                    verify { stockBriefingPostStateService.saveLastProcessedPostId(fetchedPostId) }
                    verify(exactly = 0) { publisher.publishEvent(ofType<StockBriefingContentProcessedEvent>()) }
                }
            }
        }

        Given("크롤링 중 예외가 발생하는 경우") {
            When("execute를 호출하면") {
                Then("실패 이벤트가 발행되고 postId는 저장되지 않는다") {
                    every { scrapper.scrapeStockBriefingPost(fetchedPostId) } throws RuntimeException("네트워크 오류")

                    useCase.execute()

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.failedStage == "크롤링"
                            },
                        )
                    }
                    verify(exactly = 0) { stockBriefingPostStateService.saveLastProcessedPostId(any()) }
                }
            }
        }

        Given("GPT 처리가 일부 실패하는 경우 (skip 처리)") {
            val rawContents =
                listOf(
                    StockBriefingRawContent("코스피 상승", "코스피가 올랐다."),
                    StockBriefingRawContent("나스닥 강세", "나스닥이 올랐다."),
                )

            When("execute를 호출하면") {
                Then("GPT 실패 항목은 skip 처리되어 성공한 1개만 이벤트에 포함된다") {
                    every { scrapper.scrapeStockBriefingPost(fetchedPostId) } returns rawContents
                    every { promptGenerator.toStockBriefingHeadline(any(), any()) } returns dummyPrompt
                    every { promptGenerator.toStockBriefingSummary(any(), any(), any()) } returns dummyPrompt
                    every { promptGenerator.toKoreanHighlightText(any()) } returns dummyPrompt
                    every { promptGenerator.toStockBriefingMainPageBody(any()) } returns dummyPrompt
                    // 첫 번째 컨텐츠 GPT 성공 (3번 호출), 두 번째 컨텐츠 첫 GPT 호출부터 예외 → skip
                    // mainPageBody 호출(5번째)도 throw → generateMainPageBody 내부 catch로 폴백
                    every { chatGpt.ask(dummyPrompt) } returnsMany
                        listOf(
                            Headline("코스피 2% 급등"),
                            Summary("코스피 상승."),
                            HighlightTexts(listOf("코스피")),
                        ) andThenThrows RuntimeException("GPT 호출 실패")

                    useCase.execute()

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingContentProcessedEvent> {
                                it.contents.size == 1 &&
                                    it.contents[0].headline == "코스피 2% 급등"
                            },
                        )
                    }
                }
            }
        }

        Given("GPT 처리가 전체 실패하는 경우") {
            When("execute를 호출하면") {
                Then("실패 이벤트가 발행된다") {
                    every { scrapper.scrapeStockBriefingPost(fetchedPostId) } returns
                        listOf(StockBriefingRawContent("코스피 상승", "코스피가 올랐다."))
                    every { promptGenerator.toStockBriefingHeadline(any(), any()) } returns dummyPrompt
                    every { chatGpt.ask(dummyPrompt) } throws RuntimeException("GPT 서비스 불가")

                    useCase.execute()

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.failedStage == "GPT 처리"
                            },
                        )
                    }
                    verify(exactly = 0) { stockBriefingPostStateService.saveLastProcessedPostId(any()) }
                }
            }
        }
    })