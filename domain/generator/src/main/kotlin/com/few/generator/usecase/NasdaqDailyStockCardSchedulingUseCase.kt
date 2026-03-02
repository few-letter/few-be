package com.few.generator.usecase

import com.few.generator.core.instagram.InstagramUploader
import com.few.generator.core.instagram.NasdaqDailyStockCardGenerator
import com.few.generator.core.kis.KisStockFetcher
import com.few.generator.support.aws.S3Provider
import com.few.generator.support.common.NyseMarketCalendar
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class NasdaqDailyStockCardSchedulingUseCase(
    private val kisStockFetcher: KisStockFetcher,
    private val nasdaqDailyStockCardGenerator: NasdaqDailyStockCardGenerator,
    private val s3Provider: S3Provider,
    private val instagramUploader: InstagramUploader,
    private val nyseMarketCalendar: NyseMarketCalendar,
) {
    private val log = KotlinLogging.logger {}

    fun execute() {
        val usDate = LocalDate.now(ZoneId.of("America/New_York"))
        if (!nyseMarketCalendar.isTradingDay(usDate)) {
            log.info { "NYSE 휴장일($usDate)이므로 스케줄링을 건너뜁니다." }
            return
        }

        val date = LocalDate.now()
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val outputPath = "gen_images/${dateStr}_nasdaq_daily_stock.png"

        // Step 1: KIS API로 주식 시세 조회
        log.info { "나스닥 주식 시세 조회 시작" }
        val stocks = kisStockFetcher.fetchAll()
        log.info { "주식 시세 조회 완료 (종목 수: ${stocks.values.sumOf { it.size }})" }

        // Step 2: 카드 이미지 생성
        log.info { "주식 카드 이미지 생성 시작: $outputPath" }
        val generated = nasdaqDailyStockCardGenerator.generateImage(stocks, outputPath, date)
        if (!generated) {
            throw RuntimeException("주식 카드 이미지 생성 실패: $outputPath")
        }
        log.info { "주식 카드 이미지 생성 완료: $outputPath" }

        // Step 3: S3 업로드
        log.info { "S3 업로드 시작: $outputPath" }
        val uploadResult = s3Provider.uploadImages(listOf(outputPath))

        // Step 4: 로컬 파일 삭제
        File(outputPath).takeIf { it.exists() }?.let {
            if (it.delete()) {
                log.debug { "로컬 파일 삭제 성공: $outputPath" }
            } else {
                log.warn { "로컬 파일 삭제 실패: $outputPath" }
            }
        }

        val s3Url =
            uploadResult.successfulUploads.firstOrNull()?.url
                ?: throw RuntimeException("S3 업로드 실패: ${uploadResult.getErrorMessage()}")
        log.info { "S3 업로드 완료: $s3Url" }

        // Step 5: Instagram 단일 이미지 게시
        val caption = buildCaption(date)
        log.info { "Instagram 미디어 컨테이너 생성 시작" }
        val containerId =
            instagramUploader.createSingleMediaContainer(s3Url, caption)
                ?: throw RuntimeException("Instagram 미디어 컨테이너 생성 실패: containerId가 null")

        log.info { "Instagram 게시 시작 (containerId: $containerId)" }
        instagramUploader.publishMedia(containerId)
        log.info { "Instagram 나스닥 주식 카드 게시 완료" }
    }

    private fun buildCaption(date: LocalDate): String {
        val dateFormatted = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        return """
            📈 나스닥 데일리 | $dateFormatted

            M7 · ETF 주요 종목 시황

            #나스닥 #미국주식 #NASDAQ #M7 #ETF
        """.trimIndent()
    }
}
