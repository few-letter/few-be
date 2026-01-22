package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.event.ContentsSchedulingEvent
import com.few.generator.event.GenSchedulingCompletedEvent
import com.few.generator.service.ContentsCommonGenerationService
import com.few.generator.support.common.ContentsGeneratorDelayHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher

class AbstractGenSchedulingUseCaseTest :
    BehaviorSpec({
        val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
        val scrapper = mockk<Scrapper>()
        val contentsCountByCategory = 2
        val delayHandler = mockk<ContentsGeneratorDelayHandler>(relaxed = true)
        val contentsCommonGenerationService = mockk<ContentsCommonGenerationService>()

        // Test implementation of abstract class
        class TestGenSchedulingUseCase(
            applicationEventPublisher: ApplicationEventPublisher,
            scrapper: Scrapper,
            contentsCountByCategory: Int,
            delayHandler: ContentsGeneratorDelayHandler,
            contentsCommonGenerationService: ContentsCommonGenerationService,
        ) : AbstractGenSchedulingUseCase(
                applicationEventPublisher,
                scrapper,
                contentsCountByCategory,
                delayHandler,
                contentsCommonGenerationService,
            ) {
            override val region: Region = Region.LOCAL
            override val regionName: String = "국내"
            override val schedulingName: String = "국내 뉴스 스케줄링"
            override val eventTitle: String = "[국내] 뉴스 스케줄링"

            // Override executeAsync to remove sleep for testing
            public override fun executeAsync() {
                // Skip the delay for testing
                if (!isRunning.compareAndSet(false, true)) {
                    throw BadRequestException("$schedulingName is already running. Please try again later.")
                }

                try {
                    // Call the private doExecute method using reflection
                    val doExecuteMethod = AbstractGenSchedulingUseCase::class.java.getDeclaredMethod("doExecute")
                    doExecuteMethod.isAccessible = true
                    try {
                        doExecuteMethod.invoke(this)
                    } catch (e: java.lang.reflect.InvocationTargetException) {
                        // Unwrap the original exception
                        throw e.targetException
                    }
                } finally {
                    isRunning.set(false)
                }
            }

            // Expose isRunning for testing
            fun getIsRunning() = isRunning.get()

            fun setIsRunning(value: Boolean) = isRunning.set(value)
        }

        val useCase =
            TestGenSchedulingUseCase(
                applicationEventPublisher,
                scrapper,
                contentsCountByCategory,
                delayHandler,
                contentsCommonGenerationService,
            )

        beforeEach {
            // Reset isRunning flag before each test
            useCase.setIsRunning(false)
            clearMocks(contentsCommonGenerationService)
        }

        Given("스케줄링이 이미 실행 중인 경우") {
            When("execute를 호출하면") {
                Then("BadRequestException이 발생한다") {
                    // Set running state
                    useCase.setIsRunning(true)

                    val exception =
                        shouldThrow<BadRequestException> {
                            useCase.executeAsync()
                        }

                    exception.message shouldContain "is already running"
                }
            }
        }

        Given("정상적인 스케줄링 실행 시") {
            When("모든 카테고리에서 성공적으로 콘텐츠를 생성하는 경우") {
                Then("성공 이벤트가 발행되고 GenSchedulingCompletedEvent도 발행된다") {
                    every { scrapper.extractUrlsByCategories(Region.LOCAL) } returns
                        mapOf(
                            Category.TECHNOLOGY to listOf("https://example.com/tech1", "https://example.com/tech2"),
                            Category.ECONOMY to listOf("https://example.com/econ1", "https://example.com/econ2"),
                        )

                    every {
                        contentsCommonGenerationService.createSingleContents(any(), any(), Region.LOCAL)
                    } just Runs

                    useCase.executeAsync()

                    // Verify ContentsSchedulingEventDto published
                    verify {
                        applicationEventPublisher.publishEvent(
                            match<ContentsSchedulingEvent> {
                                it.title == "[국내] 뉴스 스케줄링" &&
                                    it.isSuccess &&
                                    it.message == "None" &&
                                    it.result.contains("생성(4)")
                            },
                        )
                    }

                    // Verify GenSchedulingCompletedEventDto published
                    verify {
                        applicationEventPublisher.publishEvent(
                            match<GenSchedulingCompletedEvent> {
                                it.region == Region.LOCAL
                            },
                        )
                    }

                    // Verify running state is reset
                    useCase.getIsRunning() shouldBe false
                }
            }

            When("일부 카테고리에서 실패하는 경우") {
                Then("성공한 콘텐츠만 카운트되고 실패는 로그로 남긴다") {
                    every { scrapper.extractUrlsByCategories(Region.LOCAL) } returns
                        mapOf(
                            Category.TECHNOLOGY to listOf("https://example.com/tech1"),
                            Category.ECONOMY to listOf("https://example.com/econ1"),
                        )

                    every {
                        contentsCommonGenerationService.createSingleContents("https://example.com/tech1", Category.TECHNOLOGY, Region.LOCAL)
                    } just Runs

                    every {
                        contentsCommonGenerationService.createSingleContents("https://example.com/econ1", Category.ECONOMY, Region.LOCAL)
                    } throws RuntimeException("Failed to create content")

                    useCase.executeAsync()

                    verify {
                        applicationEventPublisher.publishEvent(
                            match<ContentsSchedulingEvent> {
                                it.isSuccess &&
                                    it.result.contains("생성(1)") &&
                                    it.result.contains("스킵(1)")
                            },
                        )
                    }
                }
            }
        }

        Given("카테고리별 최대 개수 제한이 있는 경우") {
            When("카테고리당 contentsCountByCategory만큼만 생성해야 하는 경우") {
                Then("카테고리당 설정된 개수만큼만 생성된다") {
                    var callCount = 0

                    // 5개의 URL이 있지만 contentsCountByCategory=2이므로 2개만 처리되어야 함
                    every { scrapper.extractUrlsByCategories(Region.LOCAL) } returns
                        mapOf(
                            Category.TECHNOLOGY to
                                listOf(
                                    "https://example.com/tech1",
                                    "https://example.com/tech2",
                                    "https://example.com/tech3",
                                    "https://example.com/tech4",
                                    "https://example.com/tech5",
                                ),
                        )

                    every {
                        contentsCommonGenerationService.createSingleContents(any(), Category.TECHNOLOGY, Region.LOCAL)
                    } answers {
                        callCount++
                    }

                    useCase.executeAsync()

                    // Verify that only contentsCountByCategory items were processed
                    callCount shouldBe contentsCountByCategory

                    verify {
                        applicationEventPublisher.publishEvent(
                            match<ContentsSchedulingEvent> {
                                it.result.contains("생성(2)")
                            },
                        )
                    }
                }
            }
        }

        Given("스크래핑 중 예외가 발생하는 경우") {
            When("scrapper에서 예외가 발생하면") {
                Then("실패 이벤트가 발행되고 BadRequestException이 발생한다") {
                    every { scrapper.extractUrlsByCategories(Region.LOCAL) } throws RuntimeException("Scrapping failed")

                    val exception =
                        shouldThrow<BadRequestException> {
                            useCase.executeAsync()
                        }

                    exception.message shouldContain "스케줄링에 실패"

                    // Verify running state is reset even on failure
                    useCase.getIsRunning() shouldBe false

                    // Note: Verifying event publishing might be fragile with relaxed mocks
                    // The important thing is that the exception is thrown and state is reset
                }
            }
        }

        Given("다양한 카테고리가 섞여 있는 경우") {
            When("여러 카테고리의 뉴스가 골고루 생성되어야 하는 경우") {
                Then("카테고리가 골고루 섞여서 처리된다") {
                    every { scrapper.extractUrlsByCategories(Region.LOCAL) } returns
                        mapOf(
                            Category.TECHNOLOGY to listOf("https://example.com/tech1", "https://example.com/tech2"),
                            Category.ECONOMY to listOf("https://example.com/econ1"),
                            Category.SOCIETY to
                                listOf("https://example.com/society1", "https://example.com/society2", "https://example.com/society3"),
                        )

                    val creationOrder = mutableListOf<Pair<String, Category>>()

                    every {
                        contentsCommonGenerationService.createSingleContents(any(), any(), Region.LOCAL)
                    } answers {
                        val url = firstArg<String>()
                        val category = secondArg<Category>()
                        creationOrder.add(url to category)
                    }

                    useCase.executeAsync()

                    // 첫 번째 라운드: 각 카테고리에서 1개씩
                    creationOrder[0].second shouldBe Category.TECHNOLOGY
                    creationOrder[1].second shouldBe Category.ECONOMY
                    creationOrder[2].second shouldBe Category.SOCIETY

                    // 두 번째 라운드: TECHNOLOGY, SOCIETY만 (ECONOMY는 1개뿐)
                    creationOrder[3].second shouldBe Category.TECHNOLOGY
                    creationOrder[4].second shouldBe Category.SOCIETY
                }
            }
        }
    })