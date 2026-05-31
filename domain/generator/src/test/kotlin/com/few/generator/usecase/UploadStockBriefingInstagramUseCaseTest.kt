package com.few.generator.usecase

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Keywords
import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.*
import kotlinx.coroutines.test.TestScope
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class UploadStockBriefingInstagramUseCaseTest :
    BehaviorSpec({
        val instagramUploader = mockk<com.few.generator.core.instagram.InstagramUploader>()
        val chatGpt = mockk<ChatGpt>()
        val promptGenerator = mockk<PromptGenerator>()
        val publisher = mockk<ApplicationEventPublisher>(relaxed = true)

        val useCase =
            UploadStockBriefingInstagramUseCase(
                instagramUploader = instagramUploader,
                chatGpt = chatGpt,
                promptGenerator = promptGenerator,
                applicationEventPublisher = publisher,
                scope = TestScope(),
            )

        val uploadTime = LocalDateTime.of(2025, 5, 30, 13, 10)
        val dummyPrompt = mockk<Prompt>()

        beforeEach {
            clearMocks(chatGpt, publisher)
            every { promptGenerator.toInstagramHashtags(any(), any()) } returns dummyPrompt
        }

        Given("generateCaption - 헤드라인 기반 동적 해시태그 생성") {
            val headlines = listOf("코스피 2% 급등", "나스닥 사상 최고치 경신")

            every { chatGpt.ask(dummyPrompt) } returns Keywords(listOf("코스피", "나스닥", "주식", "반도체", "증시"))

            When("generateCaption을 호출하면") {
                val caption = useCase.generateCaption(uploadTime, headlines)

                Then("날짜와 '증시 브리핑' 문구가 포함된다") {
                    caption shouldContain "5월 30일 증시 브리핑"
                }

                Then("GPT가 헤드라인 기반으로 생성한 해시태그가 포함된다") {
                    caption shouldContain "#코스피"
                    caption shouldContain "#나스닥"
                    caption shouldContain "#주식"
                }

                Then("toInstagramHashtags 호출 시 헤드라인이 입력으로 전달된다") {
                    verify {
                        promptGenerator.toInstagramHashtags(
                            match { it.containsAll(headlines) },
                            any(),
                        )
                    }
                }
            }
        }

        Given("generateCaption - 해시태그 최대 5개 제한") {
            val headlines = listOf("코스피 급등", "나스닥 강세")

            every { chatGpt.ask(dummyPrompt) } returns Keywords((1..10).map { "키워드$it" })

            When("GPT가 5개 초과 키워드를 반환해도") {
                val caption = useCase.generateCaption(uploadTime, headlines)

                Then("해시태그가 5개로 제한된다") {
                    val hashtagCount = caption.split(" ", "\n").count { it.startsWith("#") }
                    hashtagCount shouldBe 5
                }
            }
        }

        Given("generateDynamicHashtags - GPT 호출 실패 시 폴백") {
            val headlines = listOf("코스피 2% 급등")

            every { chatGpt.ask(dummyPrompt) } throws RuntimeException("GPT API 오류")

            When("GPT 호출이 실패하면") {
                val hashtags = useCase.generateDynamicHashtags(headlines)

                Then("기본 해시태그(FALLBACK_HASHTAGS)를 반환한다") {
                    hashtags shouldBe listOf("증시브리핑", "주식", "코스피", "나스닥", "주식투자")
                }
            }
        }

        Given("generateDynamicHashtags - GPT 응답이 Keywords 타입이 아닌 경우") {
            val headlines = listOf("코스피 급등")

            every { chatGpt.ask(dummyPrompt) } returns mockk()

            When("GPT 응답 캐스팅 실패 시") {
                val hashtags = useCase.generateDynamicHashtags(headlines)

                Then("기본 해시태그를 반환한다") {
                    hashtags shouldBe listOf("증시브리핑", "주식", "코스피", "나스닥", "주식투자")
                }
            }
        }

        Given("generateDynamicHashtags - 헤드라인이 비어있는 경우") {
            When("빈 헤드라인 리스트가 전달되면") {
                val hashtags = useCase.generateDynamicHashtags(emptyList())

                Then("GPT 호출 없이 기본 해시태그를 반환한다") {
                    hashtags shouldBe listOf("증시브리핑", "주식", "코스피", "나스닥", "주식투자")
                    verify(exactly = 0) { chatGpt.ask(any()) }
                }
            }
        }

        Given("generateCaption - GPT 성공 시 동적 해시태그 포함 여부") {
            val headlines = listOf("삼성전자 주가 급락", "SK하이닉스 반도체 수주")

            every { chatGpt.ask(dummyPrompt) } returns Keywords(listOf("삼성전자", "SK하이닉스", "반도체"))

            When("캡션을 생성하면") {
                val caption = useCase.generateCaption(uploadTime, headlines)

                Then("GPT 동적 해시태그가 포함되고 정적 해시태그와 다를 수 있다") {
                    caption shouldContain "#삼성전자"
                    caption shouldContain "#SK하이닉스"
                    caption shouldContain "#반도체"
                    // 동적 해시태그이므로 FALLBACK_HASHTAGS가 아닌 GPT 결과
                    caption shouldNotContain "#주식투자"
                }
            }
        }

        Given("generateCaption - 해시태그 키워드 공백 제거") {
            val headlines = listOf("코스피 상승")

            every { chatGpt.ask(dummyPrompt) } returns Keywords(listOf("삼성 전자", "SK 하이닉스"))

            When("공백이 포함된 키워드가 반환되면") {
                val caption = useCase.generateCaption(uploadTime, headlines)

                Then("공백이 제거된 해시태그가 생성된다") {
                    caption shouldContain "#삼성전자"
                    caption shouldContain "#SK하이닉스"
                    caption shouldNotContain "#삼성 전자"
                }
            }
        }

        Given("S3 업로드된 이미지가 없는 경우") {
            When("onStockBriefingS3Uploaded 이벤트 수신 시") {
                Then("실패 이벤트가 발행된다") {
                    val event =
                        com.few.generator.event.StockBriefingS3UploadedEvent(
                            postId = 2899L,
                            uploadTime = uploadTime,
                            detailImageUrls = emptyList(),
                            mainPageImageUrl = null,
                            headlines = listOf("코스피 급등"),
                        )

                    useCase.onStockBriefingS3Uploaded(event)

                    verify {
                        publisher.publishEvent(
                            match<StockBriefingInstagramUploadCompletedEvent> {
                                !it.success && it.failedStage == "S3 업로드"
                            },
                        )
                    }
                }
            }
        }
    })