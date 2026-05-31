package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository
import com.google.gson.Gson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.*

class RawContentsBrowseContentUseCaseTest :
    BehaviorSpec({
        val genRepository = mockk<GenRepository>()
        val gson = Gson()

        val useCase = RawContentsBrowseContentUseCase(genRepository, gson)

        Given("요청한 genId의 Gen이 존재하는 경우") {
            val gen =
                Gen(
                    id = 10L,
                    url = "https://n.news.naver.com/article/001/12345",
                    thumbnailImageUrl = "https://example.com/thumb.jpg",
                    mediaType = MediaType.CHOSUN.code,
                    headline = "테스트 헤드라인",
                    summary = "테스트 요약",
                    highlightTexts = """["하이라이트1", "하이라이트2"]""",
                    coreTextsJson = """["핵심1"]""",
                    category = Category.TECHNOLOGY.code,
                    region = Region.LOCAL.code,
                ).apply { createdAt = LocalDateTime.of(2026, 1, 15, 10, 0) }

            every { genRepository.findById(10L) } returns Optional.of(gen)

            When("execute를 호출하면") {
                val result = useCase.execute(10L)

                Then("Gen의 모든 필드가 올바르게 응답에 포함된다") {
                    result.id shouldBe 10L
                    result.url shouldBe "https://n.news.naver.com/article/001/12345"
                    result.thumbnailImageUrl shouldBe "https://example.com/thumb.jpg"
                    result.mediaType shouldBe MediaType.CHOSUN
                    result.headline shouldBe "테스트 헤드라인"
                    result.summary shouldBe "테스트 요약"
                    result.highlightTexts shouldBe listOf("하이라이트1", "하이라이트2")
                    result.category shouldBe Category.TECHNOLOGY
                    result.region shouldBe Region.LOCAL
                    result.createdAt shouldBe LocalDateTime.of(2026, 1, 15, 10, 0)
                }
            }
        }

        Given("요청한 genId의 Gen이 존재하지 않는 경우") {
            every { genRepository.findById(999L) } returns Optional.empty()

            When("execute를 호출하면") {
                Then("RuntimeException이 발생한다") {
                    shouldThrow<RuntimeException> {
                        useCase.execute(999L)
                    }
                }
            }
        }
    })