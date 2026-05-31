package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.core.scrapper.ScrappedResult
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository
import com.google.gson.Gson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class RawContentsServiceTest :
    BehaviorSpec({
        val scrapper = mockk<Scrapper>()
        val genRepository = mockk<GenRepository>()
        val gson = Gson()

        val service = RawContentsService(scrapper, genRepository, gson)

        val url = "https://n.news.naver.com/article/001/12345"
        val scrappedResult =
            ScrappedResult(
                sourceUrl = url,
                title = "테스트 기사 제목",
                thumbnailImageUrl = "https://example.com/thumb.jpg",
                rawTexts = listOf("본문 첫째 줄", "본문 둘째 줄"),
                images = listOf("https://example.com/img1.jpg"),
            )

        Given("URL 중복이 없는 경우") {
            every { scrapper.scrape(url) } returns scrappedResult
            every { genRepository.findByUrl(url) } returns null

            When("create를 호출하면") {
                val result = service.create(url, Category.TECHNOLOGY, Region.LOCAL)

                Then("스크래핑 결과로 RawContents VO가 반환된다") {
                    result.url shouldBe url
                    result.title shouldBe "테스트 기사 제목"
                    result.thumbnailImageUrl shouldBe "https://example.com/thumb.jpg"
                    result.rawTexts shouldBe "본문 첫째 줄\n본문 둘째 줄"
                    result.category shouldBe Category.TECHNOLOGY.code
                    result.region shouldBe Region.LOCAL.code
                }
            }
        }

        Given("동일 URL이 이미 Gen에 존재하는 경우") {
            val existingGen =
                Gen(
                    id = 1L,
                    url = url,
                    thumbnailImageUrl = null,
                    mediaType = 0,
                    headline = "기존 헤드라인",
                    summary = "기존 요약",
                    highlightTexts = "[]",
                    coreTextsJson = "[]",
                    category = Category.TECHNOLOGY.code,
                    region = Region.LOCAL.code,
                )

            every { scrapper.scrape(url) } returns scrappedResult
            every { genRepository.findByUrl(url) } returns existingGen

            When("create를 호출하면") {
                Then("BadRequestException이 발생한다") {
                    shouldThrow<BadRequestException> {
                        service.create(url, Category.TECHNOLOGY, Region.LOCAL)
                    }
                }
            }
        }
    })