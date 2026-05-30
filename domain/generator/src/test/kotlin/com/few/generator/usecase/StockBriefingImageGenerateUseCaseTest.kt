package com.few.generator.usecase

import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.core.instagram.StockBriefingContent
import com.few.generator.event.StockBriefingContentProcessedEvent
import com.few.generator.event.StockBriefingImageGeneratedEvent
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher

class StockBriefingImageGenerateUseCaseTest :
    BehaviorSpec({
        val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
        val mainPageCardGenerator = mockk<MainPageCardGenerator>()
        val publisher = mockk<ApplicationEventPublisher>(relaxed = true)

        val useCase =
            StockBriefingImageGenerateUseCase(
                singleNewsCardGenerator = singleNewsCardGenerator,
                mainPageCardGenerator = mainPageCardGenerator,
                applicationEventPublisher = publisher,
            )

        val dummyContents =
            listOf(
                StockBriefingContent(
                    headline = "코스피 2% 급등",
                    summary = "코스피가 강한 상승세를 보였습니다.",
                    highlightTexts = listOf("코스피", "2% 급등"),
                ),
                StockBriefingContent(
                    headline = "나스닥 사상 최고치 경신",
                    summary = "나스닥 지수가 사상 최고치를 돌파했습니다.",
                    highlightTexts = listOf("나스닥", "사상 최고치"),
                ),
            )

        beforeEach {
            clearMocks(singleNewsCardGenerator, mainPageCardGenerator, publisher)
        }

        Given("정상적으로 모든 이미지 생성이 성공하는 경우") {
            beforeEach {
                every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                every { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) } returns true
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 100L,
                        contents = dummyContents,
                        mainPageBody = "코스피와 나스닥이 동반 상승했습니다.",
                    )

                Then("상세 카드 이미지가 컨텐츠 수만큼 생성된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify(exactly = 2) { singleNewsCardGenerator.generateImage(any(), any()) }
                }

                Then("표지 이미지가 1개 생성된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify(exactly = 1) { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) }
                }

                Then("StockBriefingImageGeneratedEvent가 발행된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    val eventSlot = slot<StockBriefingImageGeneratedEvent>()
                    verify { publisher.publishEvent(capture(eventSlot)) }

                    val capturedEvent = eventSlot.captured
                    capturedEvent.postId shouldBe 100L
                    capturedEvent.detailImagePaths.size shouldBe 2
                    capturedEvent.headlines shouldBe listOf("코스피 2% 급등", "나스닥 사상 최고치 경신")
                }
            }
        }

        Given("상세 카드 이미지 생성이 전체 실패하는 경우") {
            beforeEach {
                every { singleNewsCardGenerator.generateImage(any(), any()) } returns false
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 101L,
                        contents = dummyContents,
                        mainPageBody = "테스트 본문",
                    )

                Then("실패 이벤트가 발행되고 표지 이미지는 생성되지 않는다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify(exactly = 0) { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) }
                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.postId == 101L && it.failedStage == "이미지 생성"
                            },
                        )
                    }
                }
            }
        }

        Given("상세 카드 이미지 일부만 성공하는 경우") {
            beforeEach {
                every {
                    singleNewsCardGenerator.generateImage(
                        match { it.headline == "코스피 2% 급등" },
                        any(),
                    )
                } returns true
                every {
                    singleNewsCardGenerator.generateImage(
                        match { it.headline == "나스닥 사상 최고치 경신" },
                        any(),
                    )
                } returns false
                every { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) } returns true
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 102L,
                        contents = dummyContents,
                        mainPageBody = "테스트 본문",
                    )

                Then("성공한 이미지만 포함하여 StockBriefingImageGeneratedEvent가 발행된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    val eventSlot = slot<StockBriefingImageGeneratedEvent>()
                    verify { publisher.publishEvent(capture(eventSlot)) }

                    eventSlot.captured.detailImagePaths.size shouldBe 1
                }
            }
        }

        Given("표지 이미지 생성이 실패하는 경우") {
            beforeEach {
                every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                every { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) } returns false
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 103L,
                        contents = dummyContents,
                        mainPageBody = "테스트 본문",
                    )

                Then("실패 이벤트가 발행된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.postId == 103L && it.failedStage == "표지 이미지 생성"
                            },
                        )
                    }
                }
            }
        }

        Given("컨텐츠가 MAX_CAROUSEL_IMAGES(9개)를 초과하는 경우") {
            val manyContents =
                (1..12).map { i ->
                    StockBriefingContent(
                        headline = "헤드라인 $i",
                        summary = "요약 $i",
                        highlightTexts = emptyList(),
                    )
                }

            beforeEach {
                every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                every { mainPageCardGenerator.generateBriefingMainPageImage(any(), any()) } returns true
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 104L,
                        contents = manyContents,
                        mainPageBody = "테스트 본문",
                    )

                Then("최대 9개까지만 상세 카드 이미지가 생성된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify(exactly = 9) { singleNewsCardGenerator.generateImage(any(), any()) }
                }
            }
        }

        Given("이미지 생성 중 예외가 발생하는 경우") {
            beforeEach {
                every { singleNewsCardGenerator.generateImage(any(), any()) } throws RuntimeException("디스크 오류")
            }

            When("StockBriefingContentProcessedEvent를 수신하면") {
                val event =
                    StockBriefingContentProcessedEvent(
                        postId = 105L,
                        contents = dummyContents,
                        mainPageBody = "테스트 본문",
                    )

                Then("실패 이벤트가 발행된다") {
                    useCase.onStockBriefingContentProcessed(event)

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.postId == 105L
                            },
                        )
                    }
                }
            }
        }
    })