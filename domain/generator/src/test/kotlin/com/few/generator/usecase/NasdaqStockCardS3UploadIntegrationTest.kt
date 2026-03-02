package com.few.generator.usecase

import com.few.generator.core.instagram.StockCardGenerator
import com.few.generator.core.kis.KisClient
import com.few.generator.core.kis.KisStockFetcher
import com.few.generator.core.kis.KisTokenClient
import com.few.generator.support.aws.S3Provider
import com.google.gson.Gson
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder
import io.awspring.cloud.s3.DiskBufferingS3OutputStreamProvider
import io.awspring.cloud.s3.S3ObjectConverter
import io.awspring.cloud.s3.S3Template
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.cloud.openfeign.support.SpringMvcContract
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 나스닥 주식 카드 이미지 생성 후 S3 업로드 통합 테스트
 *
 * 실행 조건: 아래 환경변수가 모두 설정되어야 합니다.
 *   - KIS_APP_KEY
 *   - KIS_APP_SECRET
 *   - STORAGE_ACCESS_KEY
 *   - STORAGE_SECRET_KEY
 *   - KIS_API_URL (선택, 기본값: https://openapi.koreainvestment.com:9443)
 *
 * 환경변수 미설정 시 테스트를 건너뜁니다.
 */
class NasdaqStockCardS3UploadIntegrationTest :
    FunSpec({

        val kisAppKey = System.getenv("KIS_APP_KEY") ?: ""
        val kisAppSecret = System.getenv("KIS_APP_SECRET") ?: ""
        val kisApiUrl = System.getenv("KIS_API_URL") ?: "https://openapi.koreainvestment.com:9443"
        val storageAccessKey = System.getenv("STORAGE_ACCESS_KEY") ?: ""
        val storageSecretKey = System.getenv("STORAGE_SECRET_KEY") ?: ""
        val bucket = "gen-cards"

        val gson = Gson()
        val contract = SpringMvcContract()

        val gsonEncoder =
            Encoder { obj, _, template ->
                template.body(gson.toJson(obj))
            }
        val gsonDecoder =
            Decoder { response, type ->
                response.body().asReader(Charsets.UTF_8).use { reader ->
                    gson.fromJson(reader, type)
                }
            }

        fun buildFetcher(): KisStockFetcher {
            val tokenClient =
                Feign
                    .builder()
                    .contract(contract)
                    .encoder(gsonEncoder)
                    .decoder(gsonDecoder)
                    .target(KisTokenClient::class.java, kisApiUrl)

            val stockClient =
                Feign
                    .builder()
                    .contract(contract)
                    .encoder(gsonEncoder)
                    .decoder(gsonDecoder)
                    .requestInterceptor { template ->
                        template.header("appkey", kisAppKey)
                        template.header("appsecret", kisAppSecret)
                        template.header("Content-Type", "application/json")
                    }.target(KisClient::class.java, kisApiUrl)

            return KisStockFetcher(
                kisTokenClient = tokenClient,
                kisClient = stockClient,
                appKey = kisAppKey,
                appSecret = kisAppSecret,
            )
        }

        fun buildS3Provider(): S3Provider {
            val s3Client =
                S3Client
                    .builder()
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(storageAccessKey, storageSecretKey),
                        ),
                    ).region(Region.AP_NORTHEAST_2)
                    .build()

            val s3OutputStreamProvider = DiskBufferingS3OutputStreamProvider(s3Client, null)

            val noopConverter =
                object : S3ObjectConverter {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : Any> read(
                        inputStream: InputStream,
                        clazz: Class<T>,
                    ): T = throw UnsupportedOperationException()

                    override fun <T : Any> write(obj: T): RequestBody = throw UnsupportedOperationException()

                    override fun contentType(): String = throw UnsupportedOperationException()
                }

            val s3Presigner =
                S3Presigner
                    .builder()
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(storageAccessKey, storageSecretKey),
                        ),
                    ).region(Region.AP_NORTHEAST_2)
                    .build()

            return S3Provider(S3Template(s3Client, s3OutputStreamProvider, noopConverter, s3Presigner), bucket)
        }

        test("나스닥 주식 카드 이미지를 생성하고 S3에 업로드한다") {
            if (kisAppKey.isBlank() || kisAppSecret.isBlank()) {
                println("⚠️  KIS_APP_KEY 또는 KIS_APP_SECRET 환경변수가 설정되지 않아 테스트를 건너뜁니다.")
                return@test
            }
            if (storageAccessKey.isBlank() || storageSecretKey.isBlank()) {
                println("⚠️  STORAGE_ACCESS_KEY 또는 STORAGE_SECRET_KEY 환경변수가 설정되지 않아 테스트를 건너뜁니다.")
                return@test
            }

            // Step 1: KIS API로 주식 시세 조회
            val fetcher = buildFetcher()
            val stocks = fetcher.fetchAll()
            println("=== KIS API 조회 결과 (${stocks.values.sumOf { it.size }}개) ===")
            stocks.forEach { (group, groupStocks) ->
                println("--- $group ---")
                groupStocks.forEach { stock ->
                    val arrow = when (stock.isRise) { true -> "▲"; false -> "▼"; null -> "-" }
                    println("${stock.symbol.padEnd(6)} | ${stock.koreanName.padEnd(12)} | \$${stock.currentPrice.padStart(10)} | $arrow ${stock.changeRate}%")
                }
            }

            // Step 2: 이미지 생성
            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val outputPath = "gen_images/${dateStr}_nasdaq_daily_stock_s3_test.png"

            val generator = StockCardGenerator()
            val generated = generator.generateImage(stocks, outputPath)
            println("=== 이미지 생성 결과 ===")
            println("성공 여부: $generated / 경로: ${File(outputPath).absolutePath}")
            generated shouldBe true

            // Step 3: S3 업로드
            val s3Provider = buildS3Provider()
            val uploadResult = s3Provider.uploadImages(listOf(outputPath))
            println("=== S3 업로드 결과 ===")
            println("성공: ${uploadResult.uploadedCount}개 / 실패: ${uploadResult.failedCount}개")
            uploadResult.successfulUploads.forEach { println("URL: ${it.url}") }
            uploadResult.failedUploads.forEach { println("실패: ${it.path} - ${it.errorMessage}") }

            // Step 4: 로컬 파일 정리
            File(outputPath).takeIf { it.exists() }?.delete()

            // Step 5: 검증
            uploadResult.uploadedCount shouldBe 1
            uploadResult.successfulUploads.first().url shouldNotBe null
        }
    })
