package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.service.GenService
import com.few.generator.service.instagram.NewsContent
import com.few.generator.service.instagram.SingleNewsCardGenerator
import com.google.gson.Gson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

class GenImageGenerateSchedulingUseCaseTest :
    BehaviorSpec({

        Given("정상적인 이미지 생성 시나리오") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
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
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("이미지가 생성되고 파일 경로가 반환된다") {
                    val result = useCase.execute()

                    result shouldContain "gen_image_1_"
                    result shouldContain ".png"

                    // Verify that SingleNewsCardGenerator.generateImage was called
                    val newsContentSlot = slot<NewsContent>()
                    val outputPathSlot = slot<String>()

                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(
                            capture(newsContentSlot),
                            capture(outputPathSlot),
                        )
                    }

                    // Verify NewsContent properties
                    with(newsContentSlot.captured) {
                        headline shouldBe "AI 기술의 미래"
                        summary shouldBe "인공지능 기술이 빠르게 발전하고 있습니다."
                        category shouldBe "기술"
                        createdAt shouldBe LocalDateTime.of(2026, 1, 10, 10, 0)
                        highlightTexts shouldBe listOf("인공지능", "기술")
                    }

                    // Verify output path
                    outputPathSlot.captured shouldContain "gen_image_1_"
                }
            }
        }

        Given("highlightTexts가 빈 JSON 배열인 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 2L,
                    provisioningContentsId = 2L,
                    category = Category.ECONOMY.code,
                    region = 0,
                    headline = "경제 뉴스",
                    summary = "경제 동향을 알려드립니다.",
                    highlightTexts = """[]""",
                ).apply {
                    createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("빈 highlightTexts 리스트로 이미지가 생성된다") {
                    val result = useCase.execute()

                    result shouldContain "gen_image_2_"

                    val newsContentSlot = slot<NewsContent>()
                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlot), any())
                    }

                    newsContentSlot.captured.highlightTexts shouldBe emptyList()
                }
            }
        }

        Given("highlightTexts JSON 파싱이 실패하는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 3L,
                    provisioningContentsId = 3L,
                    category = Category.POLITICS.code,
                    region = 0,
                    headline = "정치 뉴스",
                    summary = "정치 동향을 알려드립니다.",
                    highlightTexts = """invalid json""",
                ).apply {
                    createdAt = LocalDateTime.of(2026, 1, 10, 14, 0)
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("빈 highlightTexts 리스트로 이미지가 생성된다") {
                    val result = useCase.execute()

                    result shouldContain "gen_image_3_"

                    val newsContentSlot = slot<NewsContent>()
                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlot), any())
                    }

                    // JSON 파싱 실패 시 빈 리스트를 사용해야 함
                    newsContentSlot.captured.highlightTexts shouldBe emptyList()
                }
            }
        }

        Given("이미지 생성이 실패하는 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 4L,
                    provisioningContentsId = 4L,
                    category = Category.SOCIETY.code,
                    region = 0,
                    headline = "사회 뉴스",
                    summary = "사회 동향을 알려드립니다.",
                    highlightTexts = """["사회"]""",
                ).apply {
                    createdAt = LocalDateTime.of(2026, 1, 10, 16, 0)
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns false

            When("execute를 호출하면") {
                Then("RuntimeException이 발생한다") {
                    val exception =
                        shouldThrow<RuntimeException> {
                            useCase.execute()
                        }

                    exception.message shouldContain "이미지 생성 실패"
                }
            }
        }

        Given("Gen의 createdAt이 null인 경우") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 5L,
                    provisioningContentsId = 5L,
                    category = Category.LIFE.code,
                    region = 0,
                    headline = "생활 뉴스",
                    summary = "생활 정보를 알려드립니다.",
                    highlightTexts = """["생활"]""",
                )
            // createdAt을 설정하지 않음 (null)

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("현재 시간을 사용하여 이미지가 생성된다") {
                    val beforeExecution = LocalDateTime.now()
                    val result = useCase.execute()
                    val afterExecution = LocalDateTime.now()

                    result shouldContain "gen_image_5_"

                    val newsContentSlot = slot<NewsContent>()
                    verify(exactly = 1) {
                        singleNewsCardGenerator.generateImage(capture(newsContentSlot), any())
                    }

                    // createdAt이 현재 시간으로 설정되었는지 확인 (beforeExecution과 afterExecution 사이)
                    val createdAt = newsContentSlot.captured.createdAt
                    createdAt.isAfter(beforeExecution.minusSeconds(1)) shouldBe true
                    createdAt.isBefore(afterExecution.plusSeconds(1)) shouldBe true
                }
            }
        }

        Given("다양한 카테고리 코드 변환 - TECHNOLOGY") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 6L,
                    provisioningContentsId = 6L,
                    category = Category.TECHNOLOGY.code,
                    region = 0,
                    headline = "테스트",
                    summary = "테스트 요약",
                    highlightTexts = """[]""",
                ).apply {
                    createdAt = LocalDateTime.now()
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("카테고리 타이틀이 '기술'로 변환된다") {
                    useCase.execute()

                    val newsContentSlot = slot<NewsContent>()
                    verify { singleNewsCardGenerator.generateImage(capture(newsContentSlot), any()) }

                    newsContentSlot.captured.category shouldBe "기술"
                }
            }
        }

        Given("다양한 카테고리 코드 변환 - ECONOMY") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 7L,
                    provisioningContentsId = 7L,
                    category = Category.ECONOMY.code,
                    region = 0,
                    headline = "테스트",
                    summary = "테스트 요약",
                    highlightTexts = """[]""",
                ).apply {
                    createdAt = LocalDateTime.now()
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("카테고리 타이틀이 '경제'로 변환된다") {
                    useCase.execute()

                    val newsContentSlot = slot<NewsContent>()
                    verify { singleNewsCardGenerator.generateImage(capture(newsContentSlot), any()) }

                    newsContentSlot.captured.category shouldBe "경제"
                }
            }
        }

        Given("다양한 카테고리 코드 변환 - POLITICS") {
            val genService = mockk<GenService>()
            val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
            val gson = Gson()
            val useCase =
                GenImageGenerateSchedulingUseCase(
                    genService = genService,
                    singleNewsCardGenerator = singleNewsCardGenerator,
                    gson = gson,
                )

            val gen =
                Gen(
                    id = 8L,
                    provisioningContentsId = 8L,
                    category = Category.POLITICS.code,
                    region = 0,
                    headline = "테스트",
                    summary = "테스트 요약",
                    highlightTexts = """[]""",
                ).apply {
                    createdAt = LocalDateTime.now()
                }

            every { genService.findLatestGen() } returns gen
            every { singleNewsCardGenerator.generateImage(any(), any()) } returns true

            When("execute를 호출하면") {
                Then("카테고리 타이틀이 '정치'로 변환된다") {
                    useCase.execute()

                    val newsContentSlot = slot<NewsContent>()
                    verify { singleNewsCardGenerator.generateImage(capture(newsContentSlot), any()) }

                    newsContentSlot.captured.category shouldBe "정치"
                }
            }
        }
    })