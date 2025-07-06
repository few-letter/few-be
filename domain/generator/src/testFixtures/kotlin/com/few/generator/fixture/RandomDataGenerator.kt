package com.few.generator.fixture

import com.few.generator.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.domain.vo.GenDetail
import java.time.LocalDateTime
import kotlin.random.Random

object RandomDataGenerator {
    private val koreanWords =
        listOf(
            "기술",
            "혁신",
            "발전",
            "미래",
            "변화",
            "성장",
            "도전",
            "기회",
            "성공",
            "전략",
            "솔루션",
            "플랫폼",
            "시스템",
            "서비스",
            "프로젝트",
            "데이터",
            "분석",
            "개발",
            "경제",
            "사회",
            "환경",
            "문화",
            "교육",
            "건강",
            "안전",
            "품질",
            "효율성",
        )

    private val englishWords =
        listOf(
            "technology",
            "innovation",
            "development",
            "future",
            "change",
            "growth",
            "challenge",
            "opportunity",
            "success",
            "strategy",
            "solution",
            "platform",
            "system",
            "service",
            "project",
            "data",
            "analysis",
            "development",
            "economy",
            "society",
            "environment",
            "culture",
            "education",
            "health",
            "safety",
            "quality",
            "efficiency",
        )

    private val headlines =
        listOf(
            "AI 기술의 새로운 돌파구",
            "블록체인 혁신의 미래",
            "클라우드 컴퓨팅의 진화",
            "IoT 디바이스의 확산",
            "5G 네트워크 구축 가속화",
            "사이버 보안 강화 방안",
            "빅데이터 분석 기술 발전",
            "머신러닝 알고리즘 최적화",
            "자율주행차 기술 진보",
            "스마트시티 구축 프로젝트",
            "디지털 트랜스포메이션",
            "핀테크 서비스 혁신",
        )

    private val summaries =
        listOf(
            "최신 기술 동향에 대한 분석과 전망을 제시합니다.",
            "새로운 비즈니스 모델의 등장과 시장 변화를 다룹니다.",
            "디지털 혁신이 가져올 사회적 영향을 살펴봅니다.",
            "기술 발전이 우리 생활에 미치는 긍정적 효과를 설명합니다.",
            "산업 전반에 걸친 혁신 사례를 상세히 소개합니다.",
            "미래 기술 트렌드와 준비해야 할 과제를 제시합니다.",
        )

    private val highlightTexts =
        listOf(
            "혁신적인 기술 발전",
            "사용자 경험 향상",
            "비용 효율성 개선",
            "보안 수준 강화",
            "성능 최적화",
            "확장성 증대",
            "접근성 개선",
            "지속가능성 확보",
            "경쟁력 강화",
            "품질 향상",
        )

    private val keywords =
        listOf(
            "인공지능, 머신러닝, 딥러닝",
            "블록체인, 암호화폐, 스마트컨트랙트",
            "클라우드, 서버리스, 마이크로서비스",
            "IoT, 센서, 스마트디바이스",
            "5G, 네트워크, 통신",
            "보안, 암호화, 인증",
            "빅데이터, 분석, 시각화",
            "자동화, 최적화, 효율성",
            "모바일, 앱, 플랫폼",
            "디지털, 트랜스포메이션, 혁신",
        )

    fun randomKoreanWord(): String = koreanWords.random()

    fun randomEnglishWord(): String = englishWords.random()

    fun randomHeadline(): String = headlines.random() + " ${Random.nextInt(1000)}"

    fun randomSummary(): String = summaries.random() + " (${Random.nextInt(100)})"

    fun randomHighlightText(): String = highlightTexts.random() + " ${Random.nextInt(100)}"

    fun randomKeywords(): String = keywords.random()

    fun randomCategory(): Category = Category.entries.random()

    fun randomMediaType(): Int = Random.nextInt(1, 4)

    fun randomId(): Long = Random.nextLong(1, 10000)

    fun randomUrl(): String = "https://example${Random.nextInt(1000)}.com/article/${Random.nextInt(10000)}"

    fun randomDateTime(): LocalDateTime = LocalDateTime.now().minusDays(Random.nextLong(0, 30))

    fun randomCompletionId(): String = "completion-${Random.nextInt(100000)}"

    fun randomGen(
        id: Long = randomId(),
        provisioningContentsId: Long = randomId(),
        headline: String = randomHeadline(),
        summary: String = randomSummary(),
        category: Category = randomCategory(),
        highlightTexts: List<String> = listOf(randomHighlightText(), randomHighlightText()),
    ): Gen =
        Gen(
            id = id,
            provisioningContentsId = provisioningContentsId,
            completionIds = mutableListOf(randomCompletionId(), randomCompletionId(), randomCompletionId()),
            headline = headline,
            summary = summary,
            highlightTexts = """["${highlightTexts.joinToString("\", \"")}"]""",
            category = category.code,
        )

    fun randomRawContents(
        id: Long = randomId(),
        title: String = randomHeadline(),
        description: String = randomSummary(),
        category: Category = randomCategory(),
    ): RawContents =
        RawContents(
            id = id,
            url = randomUrl(),
            title = title,
            description = description,
            rawTexts = randomSummary(),
            category = category.code,
            mediaType = randomMediaType(),
        )

    fun randomProvisioningContents(
        id: Long = randomId(),
        rawContentsId: Long = randomId(),
        coreTextsJson: String? = randomSummary(),
        category: Category = randomCategory(),
    ): ProvisioningContents =
        ProvisioningContents(
            id = id,
            rawContentsId = rawContentsId,
            coreTextsJson = coreTextsJson ?: randomSummary(),
            category = category.code,
        )

    fun randomGroupGen(
        id: Long = randomId(),
        headline: String = randomHeadline(),
        summary: String = randomSummary(),
        category: Category = randomCategory(),
        selectedGroupIds: List<Int> = listOf(0, 1, 2, 3, 4).shuffled().take(Random.nextInt(2, 5)),
    ): GroupGen =
        GroupGen(
            id = id,
            category = category.code,
            selectedGroupIds = selectedGroupIds.toString(),
            headline = headline,
            summary = summary,
        )

    fun randomGenDetail(
        headline: String = randomHeadline(),
        keywords: String = randomKeywords(),
    ): GenDetail =
        GenDetail(
            headline = headline,
            keywords = keywords,
        )

    fun randomString(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun randomEmail(): String = "${randomString(8)}@example.com"

    fun randomPhoneNumber(): String = "010-${Random.nextInt(1000, 9999)}-${Random.nextInt(1000, 9999)}"

    fun randomBoolean(): Boolean = Random.nextBoolean()

    fun randomDouble(
        min: Double = 0.0,
        max: Double = 100.0,
    ): Double = Random.nextDouble(min, max)

    fun randomInt(
        min: Int = 0,
        max: Int = 100,
    ): Int = Random.nextInt(min, max)

    fun <T> randomListOf(
        generator: () -> T,
        size: Int = Random.nextInt(1, 6),
    ): List<T> = (1..size).map { generator() }

    fun randomNullableString(probability: Double = 0.5): String? = if (Random.nextDouble() < probability) randomString() else null

    fun randomException(): Exception {
        val messages =
            listOf(
                "Network timeout",
                "Database connection failed",
                "Invalid input parameter",
                "Permission denied",
                "Resource not found",
                "Service unavailable",
            )
        return RuntimeException(messages.random())
    }
}