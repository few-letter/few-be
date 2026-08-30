package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class CheckPublishableContentUseCaseTest :
    BehaviorSpec({
        val genRepository = mockk<GenRepository>()
        val useCase = CheckPublishableContentUseCase(genRepository)

        fun gen(contentsType: ContentsType?) =
            Gen(
                url = "https://example.com/${contentsType?.title ?: "none"}",
                mediaType = MediaType.CHOSUN,
                headline = "headline",
                summary = "summary",
                category = Category.TECHNOLOGY,
                region = Region.LOCAL,
                contentsType = contentsType,
            )

        Given("미발행 Gen이 존재하는 경우") {
            val gens =
                listOf(
                    gen(ContentsType.LOCAL_NEWS),
                    gen(ContentsType.LOCAL_NEWS),
                    gen(ContentsType.GLOBAL_NEWS),
                )
            every {
                genRepository.findAllByCreatedAtBetweenAndNotPublishedViaSkills(any(), any())
            } returns gens

            When("execute를 호출하면") {
                val result = useCase.execute()

                Then("hasPublishableContent=true, count=건수, contentsTypes=중복 제거된 목록을 반환한다") {
                    result.hasPublishableContent shouldBe true
                    result.count shouldBe 3
                    result.contentsTypes!!.shouldContainExactlyInAnyOrder(
                        ContentsType.LOCAL_NEWS,
                        ContentsType.GLOBAL_NEWS,
                    )
                }
            }
        }

        Given("미발행 Gen이 없는 경우") {
            every {
                genRepository.findAllByCreatedAtBetweenAndNotPublishedViaSkills(any(), any())
            } returns emptyList()

            When("execute를 호출하면") {
                val result = useCase.execute()

                Then("hasPublishableContent=false, count=0, contentsTypes=null 을 반환한다") {
                    result.hasPublishableContent shouldBe false
                    result.count shouldBe 0
                    result.contentsTypes.shouldBeNull()
                }
            }
        }

        Given("미발행 Gen 은 있으나 contents_type 이 모두 null 인 경우") {
            every {
                genRepository.findAllByCreatedAtBetweenAndNotPublishedViaSkills(any(), any())
            } returns listOf(gen(null), gen(null))

            When("execute를 호출하면") {
                val result = useCase.execute()

                Then("hasPublishableContent=true, count=2, contentsTypes=null 을 반환한다") {
                    result.hasPublishableContent shouldBe true
                    result.count shouldBe 2
                    result.contentsTypes.shouldBeNull()
                }
            }
        }
    })