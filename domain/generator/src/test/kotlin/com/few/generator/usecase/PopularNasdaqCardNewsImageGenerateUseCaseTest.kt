package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.domain.Gen
import com.few.generator.event.PopularNasdaqCardNewsImageGeneratedEvent
import com.few.generator.service.GenService
import io.kotest.core.spec.style.BehaviorSpec
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
import java.time.format.DateTimeFormatter

class PopularNasdaqCardNewsImageGenerateUseCaseTest :
    BehaviorSpec({
        val genService = mockk<GenService>()
        val singleNewsCardGenerator = mockk<SingleNewsCardGenerator>()
        val mainPageCardGenerator = mockk<MainPageCardGenerator>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

        val useCase =
            PopularNasdaqCardNewsImageGenerateUseCase(
                genService = genService,
                singleNewsCardGenerator = singleNewsCardGenerator,
                mainPageCardGenerator = mainPageCardGenerator,
                applicationEventPublisher = applicationEventPublisher,
            )

        val now = LocalDateTime.now()
        val dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        fun makeGen(
            id: Long,
            headline: String,
            summary: String = "요약",
        ) = Gen(
            id = id,
            url = "https://example.com/$id",
            mediaType = 3,
            headline = headline,
            summary = summary,
            highlightTexts = "[]",
            coreTextsJson = "[]",
            category = Category.ECONOMY.code,
            region = 0,
        ).apply { createdAt = now }

        beforeEach {
            clearMocks(genService, singleNewsCardGenerator, mainPageCardGenerator, applicationEventPublisher)
        }

        Given("genIdsByStock에 종목이 1개 있고 모든 이미지 생성이 성공하는 경우") {
            val gens =
                listOf(
                    makeGen(1L, "AAPL 헤드라인1"),
                    makeGen(2L, "AAPL 헤드라인2"),
                    makeGen(3L, "AAPL 헤드라인3"),
                    makeGen(4L, "AAPL 헤드라인4"),
                )

            When("execute를 호출하면") {
                Then("상세 카드 4개, 메인 카드 1개가 생성되고 이벤트가 발행된다") {
                    every { genService.findAllByIds(listOf(1L, 2L, 3L, 4L)) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("Apple" to listOf(1L, 2L, 3L, 4L)))

                    verify(exactly = 4) { singleNewsCardGenerator.generateImage(any(), any()) }
                    verify(exactly = 1) { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) }

                    val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
                    verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

                    eventSlot.captured.imagePathsByStock shouldContainKey "Apple"
                    eventSlot.captured.imagePathsByStock["Apple"]!!.size shouldBe 4
                    eventSlot.captured.mainPageImagePathsByStock shouldContainKey "Apple"
                }
            }
        }

        Given("상세 카드 파일 경로 패턴 검증") {
            val gens = (1L..4L).map { makeGen(it, "헤드라인$it") }

            When("execute를 호출하면") {
                Then("파일 경로가 nasdaq_{date}_{stockName}_{index}.png 패턴을 따른다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("Apple" to listOf(1L, 2L, 3L, 4L)))

                    val pathSlots = mutableListOf<String>()
                    verify(exactly = 4) { singleNewsCardGenerator.generateImage(any(), capture(pathSlots)) }

                    pathSlots[0] shouldContain "nasdaq_${dateStr}_Apple_1.png"
                    pathSlots[1] shouldContain "nasdaq_${dateStr}_Apple_2.png"
                    pathSlots[2] shouldContain "nasdaq_${dateStr}_Apple_3.png"
                    pathSlots[3] shouldContain "nasdaq_${dateStr}_Apple_4.png"
                }
            }
        }

        Given("메인 카드 타이틀과 파일 경로 패턴 검증") {
            val gens = listOf(makeGen(1L, "헤드라인"))

            When("execute를 호출하면") {
                Then("타이틀이 '{stockName} 주요소식' 이고 파일 경로가 _main.png로 끝난다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("Apple" to listOf(1L)))

                    val titleSlot = slot<String>()
                    val mainPathSlot = slot<String>()
                    verify {
                        mainPageCardGenerator.generateMainPageImage(
                            any(),
                            capture(titleSlot),
                            any(),
                            capture(mainPathSlot),
                        )
                    }

                    titleSlot.captured shouldBe "Apple 주요소식"
                    mainPathSlot.captured shouldContain "nasdaq_${dateStr}_Apple_main.png"
                }
            }
        }

        Given("종목명에 공백이 포함된 경우") {
            val gens = listOf(makeGen(1L, "헤드라인"))

            When("execute를 호출하면") {
                Then("파일 경로에서 공백이 _로 치환된다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("Nvidia Corp" to listOf(1L)))

                    val pathSlot = slot<String>()
                    verify { singleNewsCardGenerator.generateImage(any(), capture(pathSlot)) }
                    pathSlot.captured shouldContain "Nvidia_Corp"
                }
            }
        }

        Given("NewsContent의 category와 colorKey 검증") {
            val gens = listOf(makeGen(1L, "헤드라인"))

            When("execute를 호출하면") {
                Then("SingleNewsCardGenerator에는 category='nasdaq'인 NewsContent가 전달되고, MainPageCardGenerator에는 colorKey='nasdaq'이 전달된다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("TSLA" to listOf(1L)))

                    val contentSlot = slot<NewsContent>()
                    verify { singleNewsCardGenerator.generateImage(capture(contentSlot), any()) }
                    contentSlot.captured.category shouldBe "nasdaq"

                    val colorKeySlot = slot<String>()
                    verify { mainPageCardGenerator.generateMainPageImage(capture(colorKeySlot), any<String>(), any(), any()) }
                    colorKeySlot.captured shouldBe "nasdaq"
                }
            }
        }

        Given("Gen의 createdAt이 null인 경우") {
            val gen =
                Gen(
                    id = 1L,
                    url = "https://example.com/1",
                    mediaType = 3,
                    headline = "헤드라인",
                    summary = "요약",
                    highlightTexts = "[]",
                    coreTextsJson = "[]",
                    category = Category.ECONOMY.code,
                    region = 0,
                )

            When("execute를 호출하면") {
                Then("현재 시간을 createdAt으로 사용하여 이미지가 생성된다") {
                    every { genService.findAllByIds(any()) } returns listOf(gen)
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    val before = LocalDateTime.now()
                    useCase.execute(mapOf("TSLA" to listOf(1L)))
                    val after = LocalDateTime.now()

                    val contentSlot = slot<NewsContent>()
                    verify { singleNewsCardGenerator.generateImage(capture(contentSlot), any()) }
                    contentSlot.captured.createdAt.isAfter(before.minusSeconds(1)) shouldBe true
                    contentSlot.captured.createdAt.isBefore(after.plusSeconds(1)) shouldBe true
                }
            }
        }

        Given("findAllByIds가 빈 리스트를 반환하는 경우") {
            When("execute를 호출하면") {
                Then("이미지 생성과 이벤트 발행이 모두 스킵된다") {
                    every { genService.findAllByIds(any()) } returns emptyList()

                    useCase.execute(mapOf("MSFT" to listOf(1L)))

                    verify(exactly = 0) { singleNewsCardGenerator.generateImage(any(), any()) }
                    verify(exactly = 0) { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) }
                    verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
                }
            }
        }

        Given("모든 상세 카드 이미지 생성이 실패하는 경우") {
            val gens = listOf(makeGen(1L, "헤드라인"))

            When("execute를 호출하면") {
                Then("메인 이미지 생성이 호출되지 않고 이벤트도 발행되지 않는다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns false

                    useCase.execute(mapOf("GOOGL" to listOf(1L)))

                    verify(exactly = 0) { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) }
                    verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
                }
            }
        }

        Given("메인 이미지 생성이 실패하는 경우") {
            val gens = listOf(makeGen(1L, "헤드라인1"), makeGen(2L, "헤드라인2"))

            When("execute를 호출하면") {
                Then("상세 카드는 생성되었지만 해당 종목은 이벤트에 포함되지 않는다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns false

                    useCase.execute(mapOf("AMZN" to listOf(1L, 2L)))

                    verify(exactly = 2) { singleNewsCardGenerator.generateImage(any(), any()) }
                    verify(exactly = 1) { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) }
                    verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
                }
            }
        }

        Given("여러 종목이 있고 일부 종목만 메인 이미지 생성이 성공하는 경우") {
            val appleGens = listOf(makeGen(1L, "Apple 헤드라인"))
            val msftGens = listOf(makeGen(2L, "MSFT 헤드라인"))

            When("execute를 호출하면") {
                Then("성공한 종목만 이벤트에 포함되고 이벤트는 한 번 발행된다") {
                    every { genService.findAllByIds(listOf(1L)) } returns appleGens
                    every { genService.findAllByIds(listOf(2L)) } returns msftGens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every {
                        mainPageCardGenerator.generateMainPageImage(
                            any<String>(),
                            match<String> { it.contains("Apple") },
                            any(),
                            any(),
                        )
                    } returns true
                    every {
                        mainPageCardGenerator.generateMainPageImage(
                            any<String>(),
                            match<String> { it.contains("Microsoft") },
                            any(),
                            any(),
                        )
                    } returns false

                    useCase.execute(
                        mapOf(
                            "Apple" to listOf(1L),
                            "Microsoft" to listOf(2L),
                        ),
                    )

                    val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
                    verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

                    eventSlot.captured.imagePathsByStock shouldHaveSize 1
                    eventSlot.captured.imagePathsByStock shouldContainKey "Apple"
                    eventSlot.captured.mainPageImagePathsByStock shouldHaveSize 1
                    eventSlot.captured.mainPageImagePathsByStock shouldContainKey "Apple"
                }
            }
        }

        Given("genIdsByStock가 비어있는 경우") {
            When("execute를 호출하면") {
                Then("아무 작업도 수행되지 않고 이벤트도 발행되지 않는다") {
                    useCase.execute(emptyMap())

                    verify(exactly = 0) { genService.findAllByIds(any()) }
                    verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
                }
            }
        }

        Given("이벤트에 포함되는 이미지 경로 전체 구조 검증") {
            val gens = (1L..4L).map { makeGen(it, "헤드라인$it") }

            When("execute를 호출하면") {
                Then("imagePathsByStock과 mainPageImagePathsByStock이 올바르게 구성된다") {
                    every { genService.findAllByIds(any()) } returns gens
                    every { singleNewsCardGenerator.generateImage(any(), any()) } returns true
                    every { mainPageCardGenerator.generateMainPageImage(any<String>(), any<String>(), any(), any()) } returns true

                    useCase.execute(mapOf("NVDA" to listOf(1L, 2L, 3L, 4L)))

                    val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
                    verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }

                    val detailPaths = eventSlot.captured.imagePathsByStock["NVDA"]!!
                    detailPaths.size shouldBe 4
                    detailPaths.forEach { path -> path shouldContain "gen_images/" }

                    val mainPath = eventSlot.captured.mainPageImagePathsByStock["NVDA"]!!
                    mainPath shouldContain "nasdaq_${dateStr}_NVDA_main.png"
                }
            }
        }
    })