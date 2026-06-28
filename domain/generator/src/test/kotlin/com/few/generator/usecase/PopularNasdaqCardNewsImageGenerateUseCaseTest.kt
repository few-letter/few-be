package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.domain.Gen
import com.few.generator.event.PopularNasdaqCardNewsImageGeneratedEvent
import com.few.generator.service.GenService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import java.io.File
import java.time.LocalDateTime

class PopularNasdaqCardNewsImageGenerateUseCaseTest :
    FunSpec({
        val genService = mockk<GenService>()
        val singleNewsCardGenerator = SingleNewsCardGenerator()
        val mainPageCardGenerator = MainPageCardGenerator()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

        val useCase =
            PopularNasdaqCardNewsImageGenerateUseCase(
                genService = genService,
                singleNewsCardGenerator = singleNewsCardGenerator,
                mainPageCardGenerator = mainPageCardGenerator,
                applicationEventPublisher = applicationEventPublisher,
            )

        val now = LocalDateTime.now()

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

        beforeTest {
            clearMocks(genService, applicationEventPublisher)
        }

        test("Apple 종목 카드뉴스 이미지 실제 생성") {
            val gens =
                listOf(
                    makeGen(
                        1L,
                        "Apple, AI 아이폰으로 스마트폰 시장 재편 노린다",
                        "Apple이 AI 기능을 탑재한 새로운 아이폰을 출시하며 스마트폰 시장에서의 입지를 더욱 굳히고 있습니다.",
                    ),
                    makeGen(
                        2L,
                        "Apple 서비스 매출 분기 최고치 경신",
                        "Apple의 서비스 부문 매출이 분기 최고치를 기록했습니다. App Store, Apple Music, iCloud 등 구독형 서비스가 성장을 이끌었습니다.",
                    ),
                    makeGen(
                        3L,
                        "Apple Vision Pro, 기업 시장 공략 가속화",
                        "Apple이 Vision Pro를 활용한 기업용 솔루션을 강화하며 B2B 시장 진출을 본격화하고 있습니다.",
                    ),
                    makeGen(
                        4L,
                        "Apple, 자체 AI 칩 M4 탑재 맥북 라인업 공개",
                        "Apple이 자체 개발한 M4 칩을 탑재한 새로운 맥북 라인업을 공개했습니다. 기존 대비 성능이 30% 향상되었습니다.",
                    ),
                )

            every { genService.findByIdInOrderByIdAsc(listOf(1L, 2L, 3L, 4L)) } returns gens

            useCase.execute(mapOf("Apple" to listOf(1L, 2L, 3L, 4L)))

            val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
            verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

            eventSlot.captured.imagePathsByStock shouldContainKey "Apple"
            val detailPaths = eventSlot.captured.imagePathsByStock["Apple"]!!
            detailPaths.size shouldBe 4
            println("=== Apple 상세 카드 이미지 ===")
            detailPaths.forEach { path ->
                File(path).exists() shouldBe true
                println("  - $path")
            }

            val mainPath = eventSlot.captured.mainPageImagePathsByStock["Apple"]!!
            File(mainPath).exists() shouldBe true
            println("=== Apple 메인 카드 이미지 ===")
            println("  - $mainPath")

            eventSlot.captured.headlinesByStock shouldContainKey "Apple"
            eventSlot.captured.headlinesByStock["Apple"]!!.size shouldBe 4
        }

        test("NVDA 종목 카드뉴스 이미지 실제 생성") {
            val gens =
                listOf(
                    makeGen(
                        10L,
                        "NVIDIA, 블랙웰 GPU 출하량 급증…데이터센터 수요 폭발",
                        "NVIDIA의 차세대 블랙웰 아키텍처 GPU 출하량이 급증하며 AI 데이터센터 시장에서의 지배력이 강화되고 있습니다.",
                    ),
                    makeGen(
                        11L,
                        "NVIDIA CEO, AGI 달성 시점 2028년으로 전망",
                        "젠슨 황 NVIDIA CEO가 인공일반지능(AGI) 달성 시점을 2028년으로 전망했습니다.",
                    ),
                    makeGen(
                        12L,
                        "NVIDIA, 중국 수출 규제 우회 칩 개발 착수",
                        "미국의 수출 규제에 대응하기 위해 NVIDIA가 중국 시장용 특화 칩 개발에 착수했다는 보도가 나왔습니다.",
                    ),
                )

            every { genService.findByIdInOrderByIdAsc(listOf(10L, 11L, 12L)) } returns gens

            useCase.execute(mapOf("NVDA" to listOf(10L, 11L, 12L)))

            val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
            verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

            eventSlot.captured.imagePathsByStock shouldContainKey "NVDA"
            val detailPaths = eventSlot.captured.imagePathsByStock["NVDA"]!!
            detailPaths.size shouldBe 3
            println("=== NVDA 상세 카드 이미지 ===")
            detailPaths.forEach { path ->
                File(path).exists() shouldBe true
                println("  - $path")
            }

            val mainPath = eventSlot.captured.mainPageImagePathsByStock["NVDA"]!!
            File(mainPath).exists() shouldBe true
            println("=== NVDA 메인 카드 이미지 ===")
            println("  - $mainPath")

            eventSlot.captured.headlinesByStock shouldContainKey "NVDA"
            eventSlot.captured.headlinesByStock["NVDA"]!!.size shouldBe 3
        }

        test("Microsoft 종목 카드뉴스 이미지 실제 생성") {
            val gens =
                listOf(
                    makeGen(
                        20L,
                        "Microsoft Azure AI 매출 100억 달러 돌파",
                        "Microsoft의 클라우드 서비스 Azure의 AI 관련 매출이 분기 기준 100억 달러를 돌파했습니다.",
                    ),
                    makeGen(
                        21L,
                        "Microsoft, OpenAI 추가 투자…AI 패권 경쟁 가속",
                        "Microsoft가 OpenAI에 추가 투자를 단행하며 AI 시장 주도권 경쟁에서 앞서나가고 있습니다.",
                    ),
                )

            every { genService.findByIdInOrderByIdAsc(listOf(20L, 21L)) } returns gens

            useCase.execute(mapOf("Microsoft" to listOf(20L, 21L)))

            val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
            verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

            eventSlot.captured.imagePathsByStock shouldContainKey "Microsoft"
            val detailPaths = eventSlot.captured.imagePathsByStock["Microsoft"]!!
            detailPaths.size shouldBe 2
            println("=== Microsoft 상세 카드 이미지 ===")
            detailPaths.forEach { path ->
                File(path).exists() shouldBe true
                println("  - $path")
            }

            val mainPath = eventSlot.captured.mainPageImagePathsByStock["Microsoft"]!!
            File(mainPath).exists() shouldBe true
            println("=== Microsoft 메인 카드 이미지 ===")
            println("  - $mainPath")

            eventSlot.captured.headlinesByStock shouldContainKey "Microsoft"
            eventSlot.captured.headlinesByStock["Microsoft"]!!.size shouldBe 2
        }

        test("여러 종목 동시 카드뉴스 이미지 실제 생성") {
            val appleGens =
                listOf(
                    makeGen(30L, "Apple, 서비스 매출 역대 최고", "Apple 서비스 부문이 새로운 기록을 세웠습니다."),
                )
            val tslaGens =
                listOf(
                    makeGen(31L, "Tesla, 완전자율주행 FSD v13 전국 출시", "Tesla가 완전자율주행 소프트웨어 FSD v13을 전국적으로 출시했습니다."),
                    makeGen(32L, "Tesla 에너지 사업부, 분기 최고 매출 달성", "Tesla의 에너지 저장 및 태양광 사업부가 분기 최고 매출을 달성했습니다."),
                )

            every { genService.findByIdInOrderByIdAsc(listOf(30L)) } returns appleGens
            every { genService.findByIdInOrderByIdAsc(listOf(31L, 32L)) } returns tslaGens

            useCase.execute(
                mapOf(
                    "Apple" to listOf(30L),
                    "Tesla" to listOf(31L, 32L),
                ),
            )

            val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
            verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

            eventSlot.captured.imagePathsByStock shouldHaveSize 2
            eventSlot.captured.imagePathsByStock shouldContainKey "Apple"
            eventSlot.captured.imagePathsByStock shouldContainKey "Tesla"
            eventSlot.captured.headlinesByStock shouldContainKey "Apple"
            eventSlot.captured.headlinesByStock shouldContainKey "Tesla"

            println("=== 여러 종목 상세 카드 이미지 ===")
            eventSlot.captured.imagePathsByStock.forEach { (stock, paths) ->
                println("[$stock]")
                paths.forEach { path ->
                    File(path).exists() shouldBe true
                    println("  - $path")
                }
            }
            println("=== 여러 종목 메인 카드 이미지 ===")
            eventSlot.captured.mainPageImagePathsByStock.forEach { (stock, path) ->
                File(path).exists() shouldBe true
                println("[$stock] $path")
            }
        }

        test("종목명에 공백이 포함된 경우 파일 경로에서 공백이 _로 치환된다") {
            val gens = listOf(makeGen(40L, "Nvidia Corp 헤드라인", "요약 내용입니다."))

            every { genService.findByIdInOrderByIdAsc(any()) } returns gens

            useCase.execute(mapOf("Nvidia Corp" to listOf(40L)))

            val eventSlot = slot<PopularNasdaqCardNewsImageGeneratedEvent>()
            verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(eventSlot)) }

            val detailPaths = eventSlot.captured.imagePathsByStock["Nvidia Corp"]!!
            detailPaths.forEach { path ->
                path.contains("Nvidia_Corp") shouldBe true
                File(path).exists() shouldBe true
                println("공백 치환 경로: $path")
            }
        }

        test("Gen 조회 결과 없는 경우 이미지 생성 및 이벤트 발행 스킵") {
            every { genService.findByIdInOrderByIdAsc(any()) } returns emptyList()

            useCase.execute(mapOf("MSFT" to listOf(1L)))

            verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
        }

        test("genIdsByStock가 비어있는 경우 아무 작업도 수행되지 않는다") {
            useCase.execute(emptyMap())

            verify(exactly = 0) { genService.findByIdInOrderByIdAsc(any()) }
            verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
        }
    })