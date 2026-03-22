package com.few.generator.service.instagram

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.LocalDateTime

class MainPageCardGeneratorTest :
    FunSpec({

        val now = LocalDateTime.now()

        fun dummyContents(category: String) =
            listOf(
                NewsContent(
                    headline = "$category 뉴스 헤드라인 1",
                    summary = "요약 내용 1",
                    category = category,
                    createdAt = now,
                    highlightTexts = listOf("헤드라인"),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 2",
                    summary = "요약 내용 2",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 3",
                    summary = "요약 내용 3",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 4",
                    summary = "요약 내용 4",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 5",
                    summary = "요약 내용 5",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 6",
                    summary = "요약 내용 6",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
                NewsContent(
                    headline = "$category 뉴스 헤드라인 7",
                    summary = "요약 내용 7",
                    category = category,
                    createdAt = now,
                    highlightTexts = emptyList(),
                ),
            )

        test("표지 이미지 생성 테스트") {
            val generator = MainPageCardGenerator()
            val outputPath = "gen_images/test_main_page_economy.png"

            val result = generator.generateMainPageImage(Category.ECONOMY, dummyContents("경제"), Region.LOCAL, outputPath)

            result shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("모든 카테고리 표지 이미지 생성 테스트") {
            val generator = MainPageCardGenerator()
            val categories =
                listOf(
                    Category.TECHNOLOGY to "기술",
                    Category.ECONOMY to "경제",
                    Category.POLITICS to "정치",
                    Category.SOCIETY to "사회",
                    Category.LIFE to "생활",
                )

            categories.forEach { (category, categoryTitle) ->
                val outputPath = "gen_images/test_main_page_${category.englishName}.png"

                val result = generator.generateMainPageImage(category, dummyContents(categoryTitle), Region.GLOBAL, outputPath)

                result shouldBe true
                File(outputPath).exists() shouldBe true
            }
        }
    })