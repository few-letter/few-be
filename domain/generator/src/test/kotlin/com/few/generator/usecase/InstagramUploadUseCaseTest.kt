package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.domain.Gen
import com.few.generator.service.GenService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class InstagramUploadUseCaseTest :
    BehaviorSpec({
        val instagramUploader = mockk<InstagramUploader>()
        val genService = mockk<GenService>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val chatGpt = mockk<ChatGpt>()
        val promptGenerator = mockk<PromptGenerator>()

        val useCase =
            InstagramUploadUseCase(
                instagramUploader = instagramUploader,
                genService = genService,
                applicationEventPublisher = applicationEventPublisher,
                chatGpt = chatGpt,
                promptGenerator = promptGenerator,
                contentsCountByCategory = 5,
            )

        val uploadTime = LocalDateTime.of(2025, 1, 15, 10, 0)

        beforeSpec {
            every { promptGenerator.toInstagramHashtags(any(), any()) } returns mockk()
        }

        Given("generateCaption - 기본 캡션 생성") {
            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = Region.LOCAL.code,
                        headline = "앤트로픽, 크리스 리델 이사 선임",
                        summary = "앤트로픽은 크리스 리델을 이사로 선임했다고 발표했습니다.",
                        highlightTexts = """["책임 있는 AI 개발이 중요하다"]""",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                    Gen(
                        id = 2L,
                        provisioningContentsId = 2L,
                        category = Category.TECHNOLOGY.code,
                        region = Region.LOCAL.code,
                        headline = "삼성전자 새로운 반도체 공장 착공",
                        summary = "삼성전자가 새로운 반도체 공장을 착공했습니다.",
                        highlightTexts = """["삼성전자 반도체"]""",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                )

            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.TECHNOLOGY, Region.LOCAL)
            } returns gens

            every {
                chatGpt.ask(any())
            } returns Keywords(listOf("앤트로픽", "크리스리델", "AI", "삼성전자", "반도체"))

            When("TECHNOLOGY 카테고리로 캡션을 생성하면") {
                val caption = useCase.generateCaption(Category.TECHNOLOGY, Region.LOCAL, uploadTime)

                Then("제목에 날짜와 카테고리가 포함된다") {
                    caption shouldStartWith "few letter가 정리한 1월 15일의 기술 뉴스 2개"
                }

                Then("각 gen의 headline이 이모지와 함께 포함된다") {
                    caption shouldContain "🔬 앤트로픽, 크리스 리델 이사 선임"
                    caption shouldContain "🔬 삼성전자 새로운 반도체 공장 착공"
                }

                Then("GPT가 추출한 동적 해시태그만 포함된다") {
                    caption shouldContain "#앤트로픽"
                    caption shouldContain "#크리스리델"
                    caption shouldContain "#AI"
                    caption shouldContain "#삼성전자"
                    caption shouldContain "#반도체"
                }
            }
        }

        Given("generateCaption - 해시태그 최대 5개 제한") {
            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.TECHNOLOGY.code,
                        region = Region.LOCAL.code,
                        headline = "테스트 헤드라인",
                        summary = "summary",
                        highlightTexts = "[]",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                )

            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.TECHNOLOGY, Region.LOCAL)
            } returns gens

            // GPT가 10개 키워드를 반환해도 5개로 제한되어야 함
            every {
                chatGpt.ask(any())
            } returns Keywords((1..10).map { "키워드$it" })

            When("GPT가 5개 초과 키워드를 반환하는 경우") {
                val caption = useCase.generateCaption(Category.TECHNOLOGY, Region.LOCAL, uploadTime)

                Then("해시태그 수가 5개를 넘지 않는다") {
                    val hashtagCount =
                        caption
                            .split(" ", "\n")
                            .count { it.startsWith("#") }
                    hashtagCount shouldBe 5
                }

                Then("처음 5개 키워드만 포함된다") {
                    caption shouldContain "#키워드1"
                    caption shouldContain "#키워드5"
                    caption shouldNotContain "#키워드6"
                }
            }
        }

        Given("generateCaption - GPT 호출 실패 시 fallback") {
            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.ECONOMY.code,
                        region = Region.LOCAL.code,
                        headline = "한국은행 금리 인하",
                        summary = "summary",
                        highlightTexts = "[]",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                )

            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.ECONOMY, Region.LOCAL)
            } returns gens

            every {
                chatGpt.ask(any())
            } throws RuntimeException("GPT API 호출 실패")

            When("GPT 호출이 실패하면") {
                val caption = useCase.generateCaption(Category.ECONOMY, Region.LOCAL, uploadTime)

                Then("캡션이 정상적으로 생성되며 해시태그는 없다") {
                    caption shouldContain "경제 뉴스 1개"
                    caption shouldContain "💰 한국은행 금리 인하"
                    caption shouldNotContain "#"
                }
            }
        }

        Given("generateCaption - Gen이 없는 경우") {
            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.LIFE, Region.LOCAL)
            } returns emptyList()

            When("해당 카테고리의 Gen이 없으면") {
                val caption = useCase.generateCaption(Category.LIFE, Region.LOCAL, uploadTime)

                Then("뉴스 0개로 표시된다") {
                    caption shouldContain "생활 뉴스 0개"
                }

                Then("해시태그가 포함되지 않는다") {
                    caption shouldNotContain "#"
                }
            }
        }

        Given("generateCaption - GPT 응답이 Keywords로 변환 불가한 경우") {
            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.POLITICS.code,
                        region = Region.LOCAL.code,
                        headline = "대통령 국무회의 개최",
                        summary = "summary",
                        highlightTexts = "[]",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                )

            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.POLITICS, Region.LOCAL)
            } returns gens

            every {
                chatGpt.ask(any())
            } returns mockk()

            When("GPT 응답이 Keywords 타입이 아니면") {
                val caption = useCase.generateCaption(Category.POLITICS, Region.LOCAL, uploadTime)

                Then("fallback으로 해시태그 없이 캡션이 생성된다") {
                    caption shouldContain "🏛️ 대통령 국무회의 개최"
                    caption shouldNotContain "#"
                }
            }
        }

        Given("generateCaption - 카테고리별 이모지 매핑") {
            val categoryEmojiPairs =
                listOf(
                    Category.TECHNOLOGY to "🔬",
                    Category.POLITICS to "🏛️",
                    Category.ECONOMY to "💰",
                    Category.SOCIETY to "🌍",
                    Category.LIFE to "🏠",
                )

            every {
                chatGpt.ask(any())
            } returns Keywords(listOf("테스트키워드"))

            categoryEmojiPairs.forEach { (category, expectedEmoji) ->
                val gen =
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = category.code,
                        region = Region.LOCAL.code,
                        headline = "테스트 헤드라인",
                        summary = "summary",
                        highlightTexts = "[]",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    )

                every {
                    genService.findAllByCreatedAtTodayAndCategoryAndRegion(category, Region.LOCAL)
                } returns listOf(gen)

                When("${category.title} 카테고리로 캡션을 생성하면") {
                    val caption = useCase.generateCaption(category, Region.LOCAL, uploadTime)

                    Then("${category.title} 카테고리에 맞는 이모지($expectedEmoji)가 사용된다") {
                        caption shouldContain "$expectedEmoji 테스트 헤드라인"
                    }
                }
            }
        }

        Given("generateCaption - 동적 해시태그 포맷 검증") {
            val gens =
                listOf(
                    Gen(
                        id = 1L,
                        provisioningContentsId = 1L,
                        category = Category.SOCIETY.code,
                        region = Region.LOCAL.code,
                        headline = "서울시 대중교통 무료화 추진",
                        summary = "summary",
                        highlightTexts = "[]",
                        url = "https://example.com",
                        mediaType = MediaType.CHOSUN.code,
                    ),
                )

            every {
                genService.findAllByCreatedAtTodayAndCategoryAndRegion(Category.SOCIETY, Region.LOCAL)
            } returns gens

            every {
                chatGpt.ask(any())
            } returns Keywords(listOf("서울시", "대중교통", "무료화"))

            When("GPT가 키워드를 반환하면") {
                val caption = useCase.generateCaption(Category.SOCIETY, Region.LOCAL, uploadTime)

                Then("각 키워드 앞에 '#'이 붙어서 포맷팅된다") {
                    caption shouldContain "#서울시"
                    caption shouldContain "#대중교통"
                    caption shouldContain "#무료화"
                }

                Then("동적 해시태그끼리 공백으로 구분된다") {
                    caption shouldContain "#서울시 #대중교통 #무료화"
                }
            }
        }
    })