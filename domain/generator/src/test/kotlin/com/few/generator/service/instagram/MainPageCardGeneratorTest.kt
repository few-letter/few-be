package com.few.generator.service.instagram

import com.few.common.domain.Category
import com.few.generator.core.instagram.MainPageCardGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MainPageCardGeneratorTest :
    FunSpec({

        test("표지 이미지 생성 테스트") {
            val generator = MainPageCardGenerator()
            val outputPath = "gen_images/test_main_page_economy.png"

            val result = generator.generateMainPageImage(Category.ECONOMY, outputPath)

            result shouldBe true
            File(outputPath).exists() shouldBe true
        }

        test("모든 카테고리 표지 이미지 생성 테스트") {
            val generator = MainPageCardGenerator()
            val categories =
                listOf(
                    Category.TECHNOLOGY,
                    Category.ECONOMY,
                    Category.POLITICS,
                    Category.SOCIETY,
                    Category.LIFE,
                )

            categories.forEach { category ->
                val outputPath = "gen_images/test_main_page_${category.englishName}.png"

                val result = generator.generateMainPageImage(category, outputPath)

                result shouldBe true
                File(outputPath).exists() shouldBe true
            }
        }
    })