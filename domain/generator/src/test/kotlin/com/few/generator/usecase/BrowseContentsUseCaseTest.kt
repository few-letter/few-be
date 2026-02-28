package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import com.google.gson.Gson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class BrowseContentsUseCaseTest :
    BehaviorSpec({
        val genRepository = mockk<GenRepository>()
        val gson = Gson()
        val pageSize = 10

        val useCase =
            BrowseContentsUseCase(
                genRepository = genRepository,
                gson = gson,
                pageSize = pageSize,
            )

        Given("첫 페이지 조회 시 (prevGenId = -1)") {
            val input =
                BrowseContentsUseCaseIn(
                    prevGenId = -1L,
                    category = null,
                    region = Region.LOCAL,
                )

            When("Gen이 존재하지 않는 경우") {
                every {
                    genRepository.findFirstLimit(pageSize, Region.LOCAL.code)
                } returns emptyList()

                Then("빈 리스트와 isLast=true를 반환한다") {
                    val result = useCase.execute(input)

                    result.contents.shouldBeEmpty()
                    result.isLast shouldBe true
                }
            }

            When("Gen이 pageSize보다 적게 존재하는 경우") {
                val gen =
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = Region.LOCAL.code,
                        headline = "Test Headline",
                        summary = "Test Summary",
                        highlightTexts = """["highlight1", "highlight2"]""",
                        url = "https://example.com/article",
                        thumbnailImageUrl = "https://example.com/thumb.jpg",
                        mediaType = MediaType.CHOSUN.code,
                    ).apply {
                        createdAt = LocalDateTime.now()
                    }

                every {
                    genRepository.findFirstLimit(pageSize, Region.LOCAL.code)
                } returns listOf(gen)

                Then("조회된 결과와 isLast=true를 반환한다") {
                    val result = useCase.execute(input)

                    result.contents shouldHaveSize 1
                    result.isLast shouldBe true

                    with(result.contents.first()) {
                        id shouldBe 1L
                        url shouldBe "https://example.com/article"
                        thumbnailImageUrl shouldBe "https://example.com/thumb.jpg"
                        mediaType shouldBe MediaType.CHOSUN
                        headline shouldBe "Test Headline"
                        summary shouldBe "Test Summary"
                        highlightTexts shouldBe listOf("highlight1", "highlight2")
                        category shouldBe Category.TECHNOLOGY
                    }
                }
            }
        }

        Given("다음 페이지 조회 시 (prevGenId > 0)") {
            val input =
                BrowseContentsUseCaseIn(
                    prevGenId = 5L,
                    category = null,
                    region = Region.LOCAL,
                )

            When("pageSize만큼 Gen이 존재하는 경우") {
                val gens =
                    (6L..15L).map { id ->
                        Gen(
                            id = id,
                            provisioningContentsId = id,
                            category = Category.TECHNOLOGY.code,
                            region = Region.LOCAL.code,
                            headline = "Headline $id",
                            summary = "Summary $id",
                            highlightTexts = """["highlight"]""",
                            url = "https://example.com/article-$id",
                            thumbnailImageUrl = "https://example.com/thumb-$id.jpg",
                            mediaType = MediaType.CHOSUN.code,
                        ).apply {
                            createdAt = LocalDateTime.now()
                        }
                    }

                every {
                    genRepository.findNextLimit(5L, pageSize, Region.LOCAL.code)
                } returns gens

                Then("pageSize만큼 조회되고 isLast=false를 반환한다") {
                    val result = useCase.execute(input)

                    result.contents shouldHaveSize 10
                    result.isLast shouldBe false
                }
            }
        }

        Given("카테고리 필터와 함께 조회 시") {
            val input =
                BrowseContentsUseCaseIn(
                    prevGenId = -1L,
                    category = Category.TECHNOLOGY,
                    region = Region.GLOBAL,
                )

            When("카테고리에 해당하는 Gen이 존재하는 경우") {
                val gen =
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = Region.GLOBAL.code,
                        headline = "Tech Headline",
                        summary = "Tech Summary",
                        highlightTexts = """["tech highlight"]""",
                        url = "https://example.com/tech-article",
                        thumbnailImageUrl = "https://example.com/tech-thumb.jpg",
                        mediaType = MediaType.CHOSUN.code,
                    ).apply {
                        createdAt = LocalDateTime.now()
                    }

                every {
                    genRepository.findFirstLimitByCategory(Category.TECHNOLOGY.code, pageSize, Region.GLOBAL.code)
                } returns listOf(gen)

                Then("해당 카테고리의 결과만 반환한다") {
                    val result = useCase.execute(input)

                    result.contents shouldHaveSize 1
                    result.contents.first().category shouldBe Category.TECHNOLOGY
                }
            }
        }
    })