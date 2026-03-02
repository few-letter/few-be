package com.few.generator.usecase

import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.core.instagram.NasdaqDailyStockCardGenerator
import com.few.generator.core.kis.KisStockFetcher
import com.few.generator.core.kis.OverseaStockConstants
import com.few.generator.core.kis.StockQuote
import com.few.generator.support.aws.FailedUpload
import com.few.generator.support.aws.S3Provider
import com.few.generator.support.aws.S3UploadResult
import com.few.generator.support.aws.SuccessfulUpload
import com.few.generator.support.common.NyseMarketCalendar
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NasdaqDailyStockCardSchedulingUseCaseTest :
    BehaviorSpec({

        val dummyStocks =
            mapOf(
                OverseaStockConstants.StockGroup.ETF to
                    listOf(
                        StockQuote(symbol = "SPY", koreanName = "S&P500 ETF", currentPrice = "500.00", changeRate = "+0.5"),
                    ),
                OverseaStockConstants.StockGroup.M7 to
                    listOf(
                        StockQuote(symbol = "AAPL", koreanName = "애플", currentPrice = "200.00", changeRate = "+1.0"),
                    ),
            )

        Given("KIS API 조회, 이미지 생성, S3 업로드, Instagram 게시가 모두 성공하는 경우") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            val s3Url = "https://gen-cards.s3.ap-northeast-2.amazonaws.com/image.png"

            every { nyseMarketCalendar.isTradingDay(any()) } returns true
            every { kisStockFetcher.fetchAll() } returns dummyStocks
            every { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) } returns true
            every { s3Provider.uploadImages(any()) } returns
                S3UploadResult(
                    successfulUploads = listOf(SuccessfulUpload(path = "gen_images/test.png", url = s3Url)),
                    failedUploads = emptyList(),
                )
            every { instagramUploader.createSingleMediaContainer(s3Url, any()) } returns "container-id-123"
            every { instagramUploader.publishMedia("container-id-123") } returns true

            When("execute를 호출하면") {
                Then("모든 단계가 순서대로 실행된다") {
                    useCase.execute()

                    verify(exactly = 1) { kisStockFetcher.fetchAll() }
                    verify(exactly = 1) { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) }
                    verify(exactly = 1) { s3Provider.uploadImages(any()) }
                    verify(exactly = 1) { instagramUploader.createSingleMediaContainer(s3Url, any()) }
                    verify(exactly = 1) { instagramUploader.publishMedia("container-id-123") }
                }
            }
        }

        Given("이미지 생성이 실패하는 경우") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            every { nyseMarketCalendar.isTradingDay(any()) } returns true
            every { kisStockFetcher.fetchAll() } returns dummyStocks
            every { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) } returns false

            When("execute를 호출하면") {
                Then("RuntimeException이 발생하고 S3 업로드는 호출되지 않는다") {
                    shouldThrow<RuntimeException> { useCase.execute() }

                    verify(exactly = 0) { s3Provider.uploadImages(any()) }
                    verify(exactly = 0) { instagramUploader.createSingleMediaContainer(any(), any()) }
                }
            }
        }

        Given("S3 업로드가 실패하는 경우") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            every { nyseMarketCalendar.isTradingDay(any()) } returns true
            every { kisStockFetcher.fetchAll() } returns dummyStocks
            every { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) } returns true
            every { s3Provider.uploadImages(any()) } returns
                S3UploadResult(
                    successfulUploads = emptyList(),
                    failedUploads = listOf(FailedUpload(path = "gen_images/test.png", errorMessage = "upload failed")),
                )

            When("execute를 호출하면") {
                Then("RuntimeException이 발생하고 Instagram 게시는 호출되지 않는다") {
                    shouldThrow<RuntimeException> { useCase.execute() }

                    verify(exactly = 0) { instagramUploader.createSingleMediaContainer(any(), any()) }
                    verify(exactly = 0) { instagramUploader.publishMedia(any()) }
                }
            }
        }

        Given("Instagram 컨테이너 생성이 실패하는 경우 (containerId가 null)") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            val s3Url = "https://gen-cards.s3.ap-northeast-2.amazonaws.com/image.png"

            every { nyseMarketCalendar.isTradingDay(any()) } returns true
            every { kisStockFetcher.fetchAll() } returns dummyStocks
            every { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) } returns true
            every { s3Provider.uploadImages(any()) } returns
                S3UploadResult(
                    successfulUploads = listOf(SuccessfulUpload(path = "gen_images/test.png", url = s3Url)),
                    failedUploads = emptyList(),
                )
            every { instagramUploader.createSingleMediaContainer(s3Url, any()) } returns null

            When("execute를 호출하면") {
                Then("RuntimeException이 발생하고 publishMedia는 호출되지 않는다") {
                    shouldThrow<RuntimeException> { useCase.execute() }

                    verify(exactly = 0) { instagramUploader.publishMedia(any()) }
                }
            }
        }

        Given("buildCaption이 올바른 캡션을 생성하는 경우") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            val s3Url = "https://gen-cards.s3.ap-northeast-2.amazonaws.com/image.png"
            val captionSlot = mutableListOf<String>()

            every { nyseMarketCalendar.isTradingDay(any()) } returns true
            every { kisStockFetcher.fetchAll() } returns dummyStocks
            every { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) } returns true
            every { s3Provider.uploadImages(any()) } returns
                S3UploadResult(
                    successfulUploads = listOf(SuccessfulUpload(path = "gen_images/test.png", url = s3Url)),
                    failedUploads = emptyList(),
                )
            every { instagramUploader.createSingleMediaContainer(s3Url, capture(captionSlot)) } returns "container-id"
            every { instagramUploader.publishMedia(any()) } returns true

            When("execute를 호출하면") {
                Then("캡션에 '나스닥 데일리'와 해시태그가 포함된다") {
                    useCase.execute()

                    val caption = captionSlot.first()
                    caption.contains("나스닥 데일리") shouldBe true
                    caption.contains("#나스닥") shouldBe true
                    caption.contains("#NASDAQ") shouldBe true
                }
            }
        }

        Given("NYSE 휴장일인 경우") {
            val kisStockFetcher = mockk<KisStockFetcher>()
            val nasdaqDailyStockCardGenerator = mockk<NasdaqDailyStockCardGenerator>()
            val s3Provider = mockk<S3Provider>()
            val instagramUploader = mockk<InstagramUploader>()
            val nyseMarketCalendar = mockk<NyseMarketCalendar>()
            val useCase =
                NasdaqDailyStockCardSchedulingUseCase(
                    kisStockFetcher = kisStockFetcher,
                    nasdaqDailyStockCardGenerator = nasdaqDailyStockCardGenerator,
                    s3Provider = s3Provider,
                    instagramUploader = instagramUploader,
                    nyseMarketCalendar = nyseMarketCalendar,
                )

            every { nyseMarketCalendar.isTradingDay(any()) } returns false

            When("execute를 호출하면") {
                Then("모든 다운스트림 작업이 호출되지 않는다") {
                    useCase.execute()

                    verify(exactly = 0) { kisStockFetcher.fetchAll() }
                    verify(exactly = 0) { nasdaqDailyStockCardGenerator.generateImage(any(), any(), any()) }
                    verify(exactly = 0) { s3Provider.uploadImages(any()) }
                    verify(exactly = 0) { instagramUploader.createSingleMediaContainer(any(), any()) }
                    verify(exactly = 0) { instagramUploader.publishMedia(any()) }
                }
            }
        }
    })
