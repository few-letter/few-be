package com.few.generator.service.instagram

import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import io.kotest.core.spec.style.FunSpec
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
    })