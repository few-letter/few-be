package com.few.generator.service.instagram

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.domain.Gen
import com.few.generator.service.GenService
import com.few.generator.usecase.GenCardNewsImageGenerateSchedulingUseCase
import com.google.gson.Gson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.ApplicationEventPublisher
import java.io.File
import java.time.LocalDateTime

class GenCardNewsImageGenerateLocalTest :
    FunSpec({

        val singleNewsCardGenerator = SingleNewsCardGenerator()
        val mainPageCardGenerator = MainPageCardGenerator()
        val genService = mockk<GenService>()
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

        val now = LocalDateTime.now()

        test("카테고리별 카드뉴스 + 표지 이미지가 실제로 생성되는지 확인") {
            val gens =
                listOf(
                    Gen(
                        id = 100L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = 0,
                        headline = "삼성전자, AI 반도체 기술로 글로벌 시장 주도권 확보",
                        summary =
                            "삼성전자가 인공지능(AI) 전용 반도체 개발에 성공하며 차세대 기술 경쟁에서 앞서나가고 있습니다. " +
                                "새로운 AI 칩은 기존 제품 대비 성능이 2배 향상되었으며, 전력 효율성도 대폭 개선되었습니다.",
                        highlightTexts = """["삼성전자", "AI 반도체"]""",
                    ).apply { createdAt = now },
                    Gen(
                        id = 101L,
                        provisioningContentsId = 2L,
                        category = Category.TECHNOLOGY.code,
                        region = 0,
                        headline = "OpenAI, GPT-5 공개 임박…업계 판도 변화 예고",
                        summary =
                            "OpenAI가 차세대 대규모 언어 모델 GPT-5의 공개를 앞두고 있습니다. " +
                                "업계에서는 이번 모델이 기존 모델 대비 추론 능력이 크게 향상될 것으로 전망하고 있습니다.",
                        highlightTexts = """["OpenAI", "GPT-5"]""",
                    ).apply { createdAt = now },
                    Gen(
                        id = 102L,
                        provisioningContentsId = 3L,
                        category = Category.ECONOMY.code,
                        region = 0,
                        headline = "한국은행, 기준금리 동결…물가 안정 우선",
                        summary =
                            "한국은행이 기준금리를 현행 3.0%로 동결했습니다. " +
                                "물가 상승세가 둔화되고 있으나 여전히 목표치를 웃돌고 있어 신중한 통화정책을 유지하겠다는 입장입니다.",
                        highlightTexts = """["한국은행", "기준금리", "물가"]""",
                    ).apply { createdAt = now },
                    Gen(
                        id = 103L,
                        provisioningContentsId = 4L,
                        category = Category.ECONOMY.code,
                        region = 0,
                        headline = "코스피 3000 돌파…반도체·바이오 주도",
                        summary =
                            "코스피 지수가 3000선을 돌파하며 사상 최고치를 경신했습니다. " +
                                "반도체와 바이오 업종이 상승세를 이끌었으며, 외국인 투자자의 순매수가 이어지고 있습니다.",
                        highlightTexts = """["코스피", "반도체", "바이오"]""",
                    ).apply { createdAt = now },
                    Gen(
                        id = 104L,
                        provisioningContentsId = 5L,
                        category = Category.POLITICS.code,
                        region = 0,
                        headline = "여야, 민생법안 처리 합의…국회 정상화",
                        summary =
                            "여야가 민생 관련 주요 법안 처리에 합의하며 국회가 정상화되었습니다. " +
                                "양당은 경제 활성화와 사회 안전망 강화를 위한 법안을 우선 처리하기로 했습니다.",
                        highlightTexts = """["여야", "민생법안", "국회"]""",
                    ).apply { createdAt = now },
                )

            every { genService.findAllByCreatedAtBetweenAndRegion(any(), any(), Region.LOCAL) } returns gens

            val (imagesByCategory, mainPagesByCategory) = useCase.doExecute(Region.LOCAL)

            // 카드뉴스 이미지 검증
            imagesByCategory shouldHaveSize 3
            imagesByCategory shouldContainKey Category.TECHNOLOGY
            imagesByCategory shouldContainKey Category.ECONOMY
            imagesByCategory shouldContainKey Category.POLITICS
            imagesByCategory[Category.TECHNOLOGY]!!.size shouldBe 2
            imagesByCategory[Category.ECONOMY]!!.size shouldBe 2
            imagesByCategory[Category.POLITICS]!!.size shouldBe 1

            imagesByCategory.values.flatten().forEach { path ->
                File(path).exists() shouldBe true
            }

            // 표지 이미지 검증
            mainPagesByCategory shouldHaveSize 3
            mainPagesByCategory shouldContainKey Category.TECHNOLOGY
            mainPagesByCategory shouldContainKey Category.ECONOMY
            mainPagesByCategory shouldContainKey Category.POLITICS

            mainPagesByCategory.values.forEach { path ->
                File(path).exists() shouldBe true
            }

            // 콘솔에 생성된 파일 경로 출력
            println("=== 생성된 카드뉴스 이미지 ===")
            imagesByCategory.forEach { (category, paths) ->
                println("[${category.title}]")
                paths.forEach { println("  - $it") }
            }
            println("\n=== 생성된 표지 이미지 ===")
            mainPagesByCategory.forEach { (category, path) ->
                println("[${category.title}] $path")
            }
        }

        test("generateMainPageImages - 모든 카테고리 표지 이미지 실제 생성") {
            val categories =
                setOf(
                    Category.TECHNOLOGY,
                    Category.ECONOMY,
                    Category.POLITICS,
                    Category.SOCIETY,
                    Category.LIFE,
                )

            val result = useCase.generateMainPageImages(categories)

            result shouldHaveSize 5

            result.forEach { (category, path) ->
                File(path).exists() shouldBe true
                println("[${category.title}] 표지 이미지: $path")
            }
        }
    })