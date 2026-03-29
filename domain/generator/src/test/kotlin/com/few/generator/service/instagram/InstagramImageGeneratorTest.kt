package com.few.generator.service.instagram

import com.few.generator.core.instagram.ImageGeneratorUtils
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.LocalDateTime

class InstagramImageGeneratorTest :
    FunSpec({

        test("Single News Card 이미지 생성 테스트") {
            // Given
            val generator = SingleNewsCardGenerator()
            val content =
                NewsContent(
                    headline = "삼성전자, AI 반도체 기술로 글로벌 시장 주도권 확보",
                    summary =
                        "삼성전자가 인공지능(AI) 전용 반도체 개발에 성공하며 차세대 기술 경쟁에서 앞서나가고 있습니다. " +
                            "새로운 AI 칩은 기존 제품 대비 성능이 2배 향상되었으며, 전력 효율성도 대폭 개선되었습니다.",
                    category = "기술",
                    createdAt = LocalDateTime.now(),
                    highlightTexts = listOf("삼성전자", "AI 반도체", "성능 향상"),
                )
            val outputPath = "gen_images/test_single_news_card.png"

            // When
            val result = generator.generateImage(content, outputPath)

            // Then
            result shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("여러 카테고리 테스트") {
            // Given
            val generator = SingleNewsCardGenerator()
            val categories = listOf("기술", "경제", "정치", "사회", "생활")

            categories.forEach { category ->
                // Given
                val content =
                    NewsContent(
                        headline = "$category 분야의 최신 뉴스",
                        summary = "이것은 $category 카테고리의 테스트 본문입니다. 각 카테고리마다 다른 색상과 배경이 적용됩니다.",
                        category = category,
                        createdAt = LocalDateTime.now(),
                        highlightTexts = listOf(category, "최신 뉴스"),
                    )
                val outputPath = "gen_images/test_${category}_card.png"

                // When
                val result = generator.generateImage(content, outputPath)

                // Then
                result shouldBe true
                File(outputPath).exists() shouldBe true
            }
        }

        // --- resolveLineHighlights 단위 테스트 ---

        context("resolveLineHighlights") {

            test("하이라이트 전체가 라인에 포함된 경우 그대로 반환") {
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "삼성전자가 AI 반도체 개발에 성공했습니다",
                        highlightTexts = listOf("AI 반도체"),
                    )
                result shouldContainExactlyInAnyOrder listOf("AI 반도체")
            }

            test("하이라이트가 줄 끝에 걸친 경우 접두사를 반환") {
                // "AI 반도체"가 줄바꿈으로 "AI" / "반도체"로 분리된 상황에서 첫 번째 줄
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "삼성전자가 AI",
                        highlightTexts = listOf("AI 반도체"),
                    )
                result shouldContainExactlyInAnyOrder listOf("AI")
            }

            test("하이라이트가 줄 시작에 걸친 경우 접미사를 반환") {
                // "AI 반도체"가 줄바꿈으로 분리된 상황에서 두 번째 줄
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "반도체 개발에 성공했습니다",
                        highlightTexts = listOf("AI 반도체"),
                    )
                result shouldContainExactlyInAnyOrder listOf("반도체")
            }

            test("라인과 하이라이트가 전혀 관계없는 경우 빈 리스트 반환") {
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "전혀 무관한 텍스트입니다",
                        highlightTexts = listOf("AI 반도체"),
                    )
                result shouldBe emptyList()
            }

            test("하이라이트 목록이 비어있으면 빈 리스트 반환") {
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "삼성전자가 AI 반도체 개발에 성공했습니다",
                        highlightTexts = emptyList(),
                    )
                result shouldBe emptyList()
            }

            test("여러 하이라이트 혼합 - 일부는 전체 포함, 일부는 경계 걸침") {
                // 줄: "삼성전자가 AI" (하이라이트: "삼성전자", "AI 반도체")
                val result =
                    ImageGeneratorUtils.resolveLineHighlights(
                        line = "삼성전자가 AI",
                        highlightTexts = listOf("삼성전자", "AI 반도체"),
                    )
                result shouldContainExactlyInAnyOrder listOf("삼성전자", "AI")
            }
        }

        // --- 줄바꿈 경계에 걸친 하이라이트를 포함한 이미지 생성 통합 테스트 ---

        test("headline에 줄바꿈이 발생하는 highlightTexts 케이스 이미지 생성") {
            // Given
            // headline이 길어서 "AI 반도체 핵심 기술"이 줄 경계에 걸칠 수 있도록 설계
            val generator = SingleNewsCardGenerator()
            val content =
                NewsContent(
                    headline = "삼성전자, 차세대 AI 반도체 핵심 기술 확보하며 글로벌 시장 공략 본격화",
                    summary =
                        "삼성전자가 AI 반도체 핵심 기술 개발에 성공하며 차세대 경쟁에서 앞서나가고 있습니다. " +
                            "새로운 반도체는 기존 제품 대비 성능이 2배 향상되었으며, 글로벌 시장 점유율도 크게 확대될 전망입니다.",
                    category = "기술",
                    createdAt = LocalDateTime.now(),
                    highlightTexts = listOf("AI 반도체 핵심 기술", "글로벌 시장"),
                )
            val outputPath = "gen_images/test_cross_line_highlight.png"

            // When
            val result = generator.generateImage(content, outputPath)

            // Then
            result shouldBe true
            File(outputPath).exists() shouldBe true
        }
    })