package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.domain.Gen
import com.few.generator.service.GenService
import com.google.gson.Gson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class GenCardNewsImageGenerateSchedulingUseCaseTest :
    BehaviorSpec({

        Given("오늘 생성된 Gen이 여러 개 있는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = 0,
                        headline = "AI 기술의 미래",
                        summary = "인공지능 기술이 빠르게 발전하고 있습니다.",
                        highlightTexts = """["인공지능", "기술"]""",
                    ).apply {
                        createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
                    },
                    Gen(
                        id = 2L,
                        provisioningContentsId = 2L,
                        category = Category.ECONOMY.code,
                        region = 0,
                        headline = "경제 뉴스",
                        summary = "경제 동향을 알려드립니다.",
                        highlightTexts = """["경제"]""",
                    ).apply {
                        createdAt = LocalDateTime.of(2026, 1, 10, 11, 0)
                    },
                    Gen(
                        id = 3L,
                        provisioningContentsId = 3L,
                        category = Category.POLITICS.code,
                        region = 0,
                        headline = "정치 뉴스",
                        summary = "정치 동향을 알려드립니다.",
                        highlightTexts = """["정치"]""",
                    ).apply {
                        createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
                    },
                )

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns gens
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
            every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("모든 Gen에 대해 이미지가 생성되고 카테고리별 파일 경로 맵이 반환된다") {
                    val (result, mainPages) = useCase.doExecute(Region.LOCAL)

                    result shouldHaveSize 3
                    result shouldContainKey Category.TECHNOLOGY
                    result shouldContainKey Category.ECONOMY
                    result shouldContainKey Category.POLITICS
                    result[Category.TECHNOLOGY]!![0] shouldContain "20260110_technology_1.png"
                    result[Category.ECONOMY]!![0] shouldContain "20260110_economy_2.png"
                    result[Category.POLITICS]!![0] shouldContain "20260110_politics_3.png"

                    // 표지 이미지도 생성되었는지 확인
                    mainPages shouldHaveSize 3
                    mainPages shouldContainKey Category.TECHNOLOGY
                    mainPages shouldContainKey Category.ECONOMY
                    mainPages shouldContainKey Category.POLITICS

                    verify(exactly = 3) {
                        singleNewsCardGenerator.generateImage(any(), any())
                    }
                    verify(exactly = 3) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }
        }

        Given("오늘 생성된 Gen이 없는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns emptyList()

            When("execute를 호출하면") {
                Then("빈 맵이 반환된다") {
                    val (result, mainPages) = useCase.doExecute(Region.LOCAL)

                    result.shouldBeEmpty()
                    mainPages.shouldBeEmpty()

                    verify(exactly = 0) {
                        singleNewsCardGenerator.generateImage(any(), any())
                    }
                    verify(exactly = 0) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }
        }

        Given("일부 Gen의 이미지 생성이 실패하는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = 0,
                        headline = "성공 케이스",
                        summary = "성공할 이미지",
                        highlightTexts = """[]""",
                    ).apply {
                        createdAt = LocalDateTime.now()
                    },
                    Gen(
                        id = 2L,
                        provisioningContentsId = 2L,
                        category = Category.ECONOMY.code,
                        region = 0,
                        headline = "실패 케이스",
                        summary = "실패할 이미지",
                        highlightTexts = """[]""",
                    ).apply {
                        createdAt = LocalDateTime.now()
                    },
                )

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns gens
            every {
                singleNewsCardGenerator.generateImage(
                    match { it.headline == "성공 케이스" },
                    any(),
                )
            } returns true
            every {
                singleNewsCardGenerator.generateImage(
                    match { it.headline == "실패 케이스" },
                    any(),
                )
            } returns false
            every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("성공한 이미지 경로만 반환된다") {
                    val (result, mainPages) = useCase.doExecute(Region.LOCAL)

                    result shouldHaveSize 1
                    result shouldContainKey Category.TECHNOLOGY
                    result[Category.TECHNOLOGY]!![0] shouldContain "_technology_1.png"

                    // 성공한 카테고리에 대해서만 표지 이미지 생성
                    mainPages shouldHaveSize 1
                    mainPages shouldContainKey Category.TECHNOLOGY

                    verify(exactly = 2) {
                        singleNewsCardGenerator.generateImage(any(), any())
                    }
                    verify(exactly = 1) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }
        }

        Given("highlightTexts JSON 파싱이 실패하는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 1L,
                    provisioningContentsId = 1L,
                    category = Category.POLITICS.code,
                    region = 0,
                    headline = "정치 뉴스",
                    summary = "정치 동향을 알려드립니다.",
                    highlightTexts = """invalid json""",
                ).apply {
                    createdAt = LocalDateTime.now()
                }

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns listOf(gen)
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
            every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("빈 highlightTexts 리스트로 이미지가 생성된다") {
                    val (result, _) = useCase.doExecute(Region.LOCAL)

                    result shouldHaveSize 1
                    result shouldContainKey Category.POLITICS

                    val newsContentSlot = slot<NewsContent>()
                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlot), any())
                    }

                    // JSON 파싱 실패 시 빈 리스트를 사용해야 함
                    newsContentSlot.captured.highlightTexts shouldBe emptyList()
                }
            }
        }

        Given("다양한 카테고리의 Gen이 있는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = 0,
                        headline = "기술",
                        summary = "테스트",
                        highlightTexts = """[]""",
                    ).apply { createdAt = LocalDateTime.now() },
                    Gen(
                        id = 2L,
                        provisioningContentsId = 2L,
                        category = Category.ECONOMY.code,
                        region = 0,
                        headline = "경제",
                        summary = "테스트",
                        highlightTexts = """[]""",
                    ).apply { createdAt = LocalDateTime.now() },
                    Gen(
                        id = 3L,
                        provisioningContentsId = 3L,
                        category = Category.SOCIETY.code,
                        region = 0,
                        headline = "사회",
                        summary = "테스트",
                        highlightTexts = """[]""",
                    ).apply { createdAt = LocalDateTime.now() },
                )

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns gens
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
            every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("각 카테고리가 올바르게 변환되어 이미지가 생성된다") {
                    val (result, _) = useCase.doExecute(Region.LOCAL)

                    result shouldHaveSize 3
                    result shouldContainKey Category.TECHNOLOGY
                    result shouldContainKey Category.ECONOMY
                    result shouldContainKey Category.SOCIETY

                    val newsContentSlots = mutableListOf<NewsContent>()
                    verify(exactly = 3) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlots), any())
                    }

                    newsContentSlots[0].category shouldBe "기술"
                    newsContentSlots[1].category shouldBe "경제"
                    newsContentSlots[2].category shouldBe "사회"
                }
            }
        }

        Given("Gen의 createdAt이 null인 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 1L,
                    provisioningContentsId = 1L,
                    category = Category.LIFE.code,
                    region = 0,
                    headline = "생활 뉴스",
                    summary = "생활 정보를 알려드립니다.",
                    highlightTexts = """[]""",
                )
            // createdAt을 설정하지 않음 (null)

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns listOf(gen)
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
            every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("현재 시간을 사용하여 이미지가 생성된다") {
                    val beforeExecution = LocalDateTime.now()
                    val (result, _) = useCase.doExecute(Region.LOCAL)
                    val afterExecution = LocalDateTime.now()

                    result shouldHaveSize 1
                    result shouldContainKey Category.LIFE

                    val newsContentSlot = slot<NewsContent>()
                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlot), any())
                    }

                    // createdAt이 현재 시간으로 설정되었는지 확인
                    val createdAt = newsContentSlot.captured.createdAt
                    createdAt.isAfter(beforeExecution.minusSeconds(1)) shouldBe true
                    createdAt.isBefore(afterExecution.plusSeconds(1)) shouldBe true
                }
            }
        }

        Given("generateMainPageImages 메서드 테스트") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val mainPageCardGenerator = mockk<MainPageCardGenerator>()
            val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val gson = Gson()
            val useCase =
                GenCardNewsImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    mainPageCardGenerator = mainPageCardGenerator,
                    applicationEventPublisher = applicationEventPublisher,
                    gson = gson,
                )

            When("모든 카테고리의 표지 이미지 생성이 성공하면") {
                every { mainPageCardGenerator.generateMainPageImage(any(), any()) } returns true

                Then("모든 카테고리에 대해 표지 이미지 경로가 반환된다") {
                    val categories = setOf(Category.TECHNOLOGY, Category.ECONOMY, Category.POLITICS)
                    val result = useCase.generateMainPageImages(categories)

                    result shouldHaveSize 3
                    result shouldContainKey Category.TECHNOLOGY
                    result shouldContainKey Category.ECONOMY
                    result shouldContainKey Category.POLITICS
                    result[Category.TECHNOLOGY]!! shouldContain "_technology_main.png"
                    result[Category.ECONOMY]!! shouldContain "_economy_main.png"
                    result[Category.POLITICS]!! shouldContain "_politics_main.png"

                    verify(exactly = 3) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }

            When("일부 카테고리의 표지 이미지 생성이 실패하면") {
                clearMocks(mainPageCardGenerator)
                every {
                    mainPageCardGenerator.generateMainPageImage(
                        match { it == Category.TECHNOLOGY },
                        any(),
                    )
                } returns true
                every {
                    mainPageCardGenerator.generateMainPageImage(
                        match { it == Category.ECONOMY },
                        any(),
                    )
                } returns false

                Then("성공한 카테고리만 반환된다") {
                    val categories = setOf(Category.TECHNOLOGY, Category.ECONOMY)
                    val result = useCase.generateMainPageImages(categories)

                    result shouldHaveSize 1
                    result shouldContainKey Category.TECHNOLOGY

                    verify(exactly = 2) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }

            When("빈 카테고리 셋이 주어지면") {
                clearMocks(mainPageCardGenerator)

                Then("빈 맵이 반환된다") {
                    val result = useCase.generateMainPageImages(emptySet())

                    result.shouldBeEmpty()

                    verify(exactly = 0) {
                        mainPageCardGenerator.generateMainPageImage(any(), any())
                    }
                }
            }

            When("표지 이미지 생성 중 예외가 발생하면") {
                clearMocks(mainPageCardGenerator)
                every {
                    mainPageCardGenerator.generateMainPageImage(
                        match { it == Category.TECHNOLOGY },
                        any(),
                    )
                } throws RuntimeException("이미지 생성 오류")
                every {
                    mainPageCardGenerator.generateMainPageImage(
                        match { it == Category.ECONOMY },
                        any(),
                    )
                } returns true

                Then("예외가 발생한 카테고리를 제외하고 나머지가 반환된다") {
                    val categories = setOf(Category.TECHNOLOGY, Category.ECONOMY)
                    val result = useCase.generateMainPageImages(categories)

                    result shouldHaveSize 1
                    result shouldContainKey Category.ECONOMY
                }
            }
        }
    })