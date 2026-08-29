package com.few.generator.service.instagram

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import com.few.generator.core.instagram.CardImageGeneratorUtils
import com.few.generator.core.instagram.CategoryConstants
import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDateTime
import javax.imageio.ImageIO

/**
 * "메인 카드는 정상 / 상세 카드에서 특정 글자가 누락·깨짐" 케이스 재현 테스트.
 *
 * ── 관찰된 케이스 ────────────────────────────────────────────────────────
 *  case0 SK Hynix : summary 본문의 숫자 토큰(40억 / 1,000개 / 2030년 ...)
 *  case1 마벨      : headline "마벨 주가 8% 하락, 2분기 매출 37% 증가"  → 깨진 문자 '8'
 *  case2 국민의힘  : headline "국민의힘 28일 결의문 채택"               → 깨진 문자 '8'
 *  case3 박근혜    : headline "박근혜, 당 의견 차이 언급"               → 깨진 문자 ','
 *  case4 골드만삭스: headline "골드만삭스, 걸프 석유 수출 60% 증가"      → 깨진 문자 '0'
 *
 * ── 원인 (독립적인 두 가지) ─────────────────────────────────────────────
 *  (A) 하이라이트 조각화 (방안 1 로 해결)
 *      상세 카드(SingleNewsCardGenerator)는 headline 을 `drawMultilineHighlightedText` 로 그리며,
 *      `wrapText` 후 각 줄을 **highlightTexts(=summary 기준) 경계마다 조각내어** `drawString` 을
 *      반복 호출한다. `resolveLineHighlights` 가 headline 과 무관한 하이라이트의 접두/접미사까지
 *      경계로 잡아(예: headline "…2분기" 가 summary 하이라이트 "2분기 매출은…" 의 접두사와 일치)
 *      숫자/문장부호 한 글자가 조각 경계에 걸려 앞 글자와 겹치거나 형광펜 배경에 덮인다.
 *      → 메인 카드는 headline 을 `drawString` 1회로 그리므로 이 문제가 없다.
 *
 *  (B) 폰트 대체로 인한 글리프 누락 (방안 4 로 해결)
 *      기존 `CardImageGeneratorUtils` 는 `Font("Noto Sans CJK KR", …)` 로 이름 기반 로드했다.
 *      해당 패밀리가 없으면 java.awt 가 조용히 논리폰트 `Dialog` 로 대체하고,
 *      `Dialog` 합성폰트는 한글에 인접한 ASCII 글리프(`0` `%` `,` `8` 등)를 **렌더에서 누락**시키면서
 *      `stringWidth` 는 그대로 유지한다. `drawString("60%")` 한 번만 호출해도 "0%" 가 사라진다.
 *      (검증: Nanum Gothic / Apple SD Gothic Neo 등 물리 폰트는 정상, Dialog 만 누락)
 *      `canDisplayUpTo("한글테스트") == -1` 가드는 Dialog 도 한글 표시가 가능하므로 이를 못 잡는다.
 *
 * case4 는 headline 에 어떤 highlightTexts 도 포함되지 않아 (A) 방안1 필터를 통과하지만
 * (headline 이 drawString 1회로 그려짐) 여전히 '0' 이 사라진다 → 순수 (B) 케이스.
 *
 * ── 적용된 수정 ─────────────────────────────────────────────────────────
 *  방안 1: SingleNewsCardGenerator 는 headline 을 그릴 때 `content.highlightTexts` 중
 *          **headline 문자열에 실제로 등장하는 것만** 사용한다.
 *  방안 4: CardImageGeneratorUtils 는 Noto Sans KR 정적 weight 파일(resources/fonts/NotoSansKR-*.ttf)을
 *          `Font.createFont` 로 직접 로드한다. OS 폰트 이름 resolve / Dialog 대체를 타지 않는다.
 *
 * 각 케이스마다 메인/상세 카드와 headline 비교 스트립을 실제로 생성해 육안 비교할 수 있게 저장한다.
 *  - gen_images/repro_{case}_main.png
 *  - gen_images/repro_{case}_detail.png
 *  - gen_images/repro_{case}_headline.png   (①통짜 / ②수정후(필터된 하이라이트) / ③수정전(원본 하이라이트))
 */
class CardNewsGlyphRenderingTest :
    FunSpec({

        data class Case(
            val name: String,
            val headline: String,
            val highlightTexts: List<String>,
            val summary: String,
            val region: Region,
            val mediaType: MediaType,
            val category: Category,
            val brokenChar: String,
        )

        val cases =
            listOf(
                Case(
                    name = "case0_skhynix",
                    headline = "SK Hynix 인디애나 공장 2030년 완공",
                    highlightTexts =
                        listOf(
                            "40억 달러 규모의 공장",
                            "약 1,000개의 직접 일자리를 창출",
                            "총 투자액은 2030년까지 450억 달러를 초과",
                        ),
                    summary =
                        "SK Hynix는 인디애나에 40억 달러 규모의 공장을 건설하여 2030년까지 미국의 주요 고대역폭 " +
                            "메모리(HBM) 생산 기지로 자리매김할 계획입니다. 이 공장은 약 1,000개의 직접 일자리를 " +
                            "창출하며, 6,000개의 추가 일자리를 유발할 것으로 예상됩니다. 또한, SK Hynix의 미국 내 " +
                            "총 투자액은 2030년까지 450억 달러를 초과할 것으로 보입니다.",
                    region = Region.from(1),
                    mediaType = MediaType.from(-1),
                    category = Category.from(2),
                    brokenChar = "(summary 숫자 토큰)",
                ),
                Case(
                    name = "case1_marvell",
                    headline = "마벨 주가 8% 하락, 2분기 매출 37% 증가",
                    highlightTexts =
                        listOf(
                            "마벨 주가는 2분기 매출이 37% 증가했음에도 불구하고 8% 하락했습니다.",
                            "2분기 매출은 27억 달러로, 이전 예상치를 3900만 달러 초과했습니다.",
                            "2028 회계연도 전망이 투자자 기대에 미치지 못해 부정적인 반응을 이끌어냈습니다.",
                        ),
                    summary =
                        "마벨 주가는 2분기 매출이 37% 증가했음에도 불구하고 8% 하락했습니다. 2분기 매출은 27억 " +
                            "달러로, 이전 예상치를 3900만 달러 초과했습니다. 그러나 2028 회계연도 전망이 투자자 " +
                            "기대에 미치지 못해 부정적인 반응을 이끌어냈습니다. 마벨은 연간 50% 성장하여 약 180억 " +
                            "달러의 매출을 예상하고 있습니다.",
                    region = Region.from(1),
                    mediaType = MediaType.from(-1),
                    category = Category.from(2),
                    brokenChar = "8",
                ),
                Case(
                    name = "case2_ppp",
                    headline = "국민의힘 28일 결의문 채택",
                    highlightTexts =
                        listOf(
                            "국민의힘은 28일 경기도 고양시에서 열린 연찬회에서 결의문을 채택하였습니다.",
                            "이재명 정권의 법치주의 훼손과 불균형 정책을 비판하며, 법치와 안보를 지키겠다고 다짐했습니다.",
                            "청년 일자리 창출을 약속하였습니다.",
                        ),
                    summary =
                        "국민의힘은 28일 경기도 고양시에서 열린 연찬회에서 결의문을 채택하였습니다. 의원들은 이재명 " +
                            "정권의 법치주의 훼손과 불균형 정책을 비판하며, 법치와 안보를 지키겠다고 다짐했습니다. " +
                            "또한, 민생을 위한 대안을 제시하고 청년 일자리 창출을 약속하였습니다. 장동혁 대표는 " +
                            "국민과의 약속을 지키는 것이 중요하다고 강조했습니다.",
                    region = Region.from(0),
                    mediaType = MediaType.from(5),
                    category = Category.from(8),
                    brokenChar = "8",
                ),
                Case(
                    name = "case3_parkgeunhye",
                    headline = "박근혜, 당 의견 차이 언급",
                    highlightTexts =
                        listOf(
                            "박근혜 전 대통령은 국민의힘 연찬회에서",
                            "유영하 의원은 이 발언을 통해",
                            "유승민 전 의원의 요청에 대한 답변이 없음을 언급하며",
                        ),
                    summary =
                        "박근혜 전 대통령은 국민의힘 연찬회에서 당 대표와 원내대표 간 의견 차이를 ‘집이 잘 되기 위한 " +
                            "것’이라고 언급했습니다. 유영하 의원은 이 발언을 통해 부부 간의 의견 대립을 비유하며, 박 전 " +
                            "대통령이 특정인을 지원하기 위해 참석한 것은 아니라고 강조했습니다. 또한, 유승민 전 의원의 " +
                            "요청에 대한 답변이 없음을 언급하며, 탄핵에 대한 자신의 입장을 명확히 할 것을 촉구했습니다.",
                    region = Region.from(0),
                    mediaType = MediaType.from(10),
                    category = Category.from(8),
                    brokenChar = ",",
                ),
                Case(
                    // headline 에 어떤 highlightTexts 도 포함되지 않아 방안1 필터를 통과해도
                    // (headline 은 drawMultilineText / drawString 1회로 그려짐) 여전히 '0' 이 사라진다.
                    // → 조각화가 아니라 폰트(Dialog 대체) 레벨의 글리프 누락임을 보여주는 케이스.
                    name = "case4_goldman",
                    headline = "골드만삭스, 걸프 석유 수출 60% 증가",
                    highlightTexts =
                        listOf(
                            "골드만삭스의 추정에 따르면",
                            "걸프 지역의 석유 수출량이 이란 전쟁 이전 수준의 60%를 초과하였습니다.",
                            "글로벌 에너지 시장에서 걸프 국가들의 영향력이 더욱 강화되고 있음을 나타냅니다.",
                        ),
                    summary =
                        "골드만삭스의 추정에 따르면, 걸프 지역의 석유 수출량이 이란 전쟁 이전 수준의 60%를 " +
                            "초과하였습니다. 이는 글로벌 에너지 시장에서 걸프 국가들의 영향력이 더욱 강화되고 " +
                            "있음을 나타냅니다.",
                    region = Region.from(1),
                    mediaType = MediaType.from(-1),
                    category = Category.from(16),
                    brokenChar = "0",
                ),
            )

        // SingleNewsCardGenerator 의 headline 렌더 파라미터와 동일하게 맞춘 상수
        val headlineFontSize = 80
        val contentWidth = 920
        val marginX = 80
        val headlineLineSpacing = 1.25f

        cases.forEach { c ->
            val content =
                NewsContent(
                    headline = c.headline,
                    summary = c.summary,
                    category = c.category.title,
                    createdAt = LocalDateTime.now(),
                    highlightTexts = c.highlightTexts,
                )

            test("[${c.name}] 메인 + 상세 카드 이미지 생성 (깨진 문자: '${c.brokenChar}')") {
                val mainPath = "gen_images/repro_${c.name}_main.png"
                val detailPath = "gen_images/repro_${c.name}_detail.png"

                val mainOk =
                    MainPageCardGenerator().generateMainPageImage(
                        category = c.category,
                        newsContents = listOf(content),
                        region = c.region,
                        outputPath = mainPath,
                    )
                val detailOk = SingleNewsCardGenerator().generateImage(content, detailPath)

                mainOk shouldBe true
                detailOk shouldBe true
                File(mainPath).exists() shouldBe true
                File(detailPath).exists() shouldBe true

                println(
                    "[${c.name}] region=${c.region.name} media=${c.mediaType.title} category=${c.category.title} " +
                        "brokenChar='${c.brokenChar}'",
                )
                println("[${c.name}] main   -> ${File(mainPath).absolutePath}")
                println("[${c.name}] detail -> ${File(detailPath).absolutePath}")
            }

            test("[${c.name}] headline 렌더 비교 + 방안1 필터 검증") {
                val font = CardImageGeneratorUtils.loadKoreanFont(headlineFontSize, bold = true)
                val themeColor = CategoryConstants.getCategoryColor(c.category.englishName).toColor()

                // 방안 1: 수정된 SingleNewsCardGenerator 와 동일한 필터
                val headlineHighlights =
                    c.highlightTexts.filter { it.isNotBlank() && c.headline.contains(it) }

                withClue("headline 에 실제로 등장하지 않는 하이라이트가 headline 렌더에 넘어가고 있음") {
                    headlineHighlights shouldBe emptyList()
                }

                // --- wrapText / resolveLineHighlights 로 조각화 상태 로그 (수정 전 vs 후) ---
                val probe = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB).createGraphics()
                CardImageGeneratorUtils.setupGraphics(probe)
                probe.font = font
                val lines = CardImageGeneratorUtils.wrapText(probe, c.headline, font, contentWidth)
                val fm = probe.fontMetrics

                println("=== [${c.name}] headline='${c.headline}' / 깨진 문자='${c.brokenChar}' ===")
                println("폰트 family='${font.family}'  wrap ${lines.size}줄")
                var active = emptySet<String>()
                lines.forEachIndexed { i, line ->
                    val fragsBefore = CardImageGeneratorUtils.resolveLineHighlights(line, c.highlightTexts, active)
                    val fragsAfter = CardImageGeneratorUtils.resolveLineHighlights(line, headlineHighlights, active)
                    println("  line[$i] \"$line\"")
                    println("    수정전 조각 = $fragsBefore  (Δ=${fragmentedWidth(fm, line, fragsBefore) - fm.stringWidth(line)}px)")
                    println("    수정후 조각 = $fragsAfter")
                    active = nextActiveHighlights(line, c.highlightTexts, active)
                }
                probe.dispose()

                // --- 비교 스트립: ①통짜  ②수정후(필터)  ③수정전(원본) ---
                val lineH = (fm.height * headlineLineSpacing).toInt()
                val block = lineH * lines.size + 60
                val strip = BufferedImage(1080, block * 3 + 40, BufferedImage.TYPE_INT_RGB)
                val g = strip.createGraphics()
                CardImageGeneratorUtils.setupGraphics(g)
                g.color = CategoryConstants.getCategoryBgColor(c.category.englishName).toColor()
                g.fillRect(0, 0, strip.width, strip.height)

                fun label(
                    text: String,
                    y: Int,
                ) {
                    g.color = Color(90, 90, 90)
                    g.font = CardImageGeneratorUtils.loadKoreanFont(24, bold = false)
                    g.drawString(text, marginX, y)
                }

                // ① 통짜
                label("① 통짜 drawString (메인 카드 방식)", 34)
                g.font = font
                var y = 34 + fm.ascent
                lines.forEach { line ->
                    g.color = Color(12, 18, 27)
                    g.drawString(line, marginX, y)
                    y += lineH
                }

                // ② 수정후: 필터된 하이라이트 (이 케이스들은 빈 리스트 → 통짜와 동일)
                label("② drawMultilineHighlightedText + 방안1 필터 (수정 후 상세 카드)", block + 34)
                CardImageGeneratorUtils.drawMultilineHighlightedText(
                    graphics = g,
                    text = c.headline,
                    highlightTexts = headlineHighlights,
                    x = marginX,
                    startY = block + 34 + fm.ascent,
                    maxWidth = contentWidth,
                    font = font,
                    normalColor = Color(12, 18, 27),
                    highlightColor = themeColor,
                    lineSpacing = headlineLineSpacing,
                )

                // ③ 수정전: summary 하이라이트 원본 (무관 어절이 조각/강조됨)
                label("③ drawMultilineHighlightedText + 원본 highlightTexts (수정 전 - 버그)", block * 2 + 34)
                CardImageGeneratorUtils.drawMultilineHighlightedText(
                    graphics = g,
                    text = c.headline,
                    highlightTexts = c.highlightTexts,
                    x = marginX,
                    startY = block * 2 + 34 + fm.ascent,
                    maxWidth = contentWidth,
                    font = font,
                    normalColor = Color(12, 18, 27),
                    highlightColor = themeColor,
                    lineSpacing = headlineLineSpacing,
                )
                g.dispose()

                val cmpPath = "gen_images/repro_${c.name}_headline.png"
                File(cmpPath).parentFile?.mkdirs()
                ImageIO.write(strip, "PNG", File(cmpPath))
                println("  headline 비교 스트립 -> ${File(cmpPath).absolutePath}")
            }
        }

        test("방안4 검증 - 카드 폰트가 번들된 Noto Sans KR 로 로드된다") {
            val regular = CardImageGeneratorUtils.loadKoreanFont(48, bold = false)
            val bold = CardImageGeneratorUtils.loadKoreanFont(48, bold = true)
            println("regular family='${regular.family}' name='${regular.fontName}'")
            println("bold    family='${bold.family}' name='${bold.fontName}'")

            withClue("번들 폰트(resources/fonts/NotoSansKR-*.ttf)가 로드되지 않고 OS 논리폰트로 대체됨") {
                regular.family shouldBe "Noto Sans KR"
                bold.family shouldBe "Noto Sans KR"
            }
            // 카드에 쓰이는 한글 + 숫자 + 문장부호 전부 표시 가능해야 한다.
            regular.canDisplayUpTo("한글 60% 8% 28일 1,000개 450억 · ‘’") shouldBe -1
        }

        test("원인(B) 회귀 방지 - headline drawString 1회 렌더에서 글리프 누락 없음") {
            val font = CardImageGeneratorUtils.loadKoreanFont(headlineFontSize, bold = true)
            // 정상 자간/어절공백은 폰트크기의 ~0.5배 이하. 글리프가 통째로 빠지면 그보다 커진다.
            val gapThreshold = (headlineFontSize * 0.5).toInt()

            val probe = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB).createGraphics()
            CardImageGeneratorUtils.setupGraphics(probe)
            probe.font = font

            println("=== 원인(B) 회귀 방지 (font family='${font.family}', 임계 ${gapThreshold}px) ===")
            val dropped = mutableListOf<String>()
            cases
                .filter { asciiInHangulTokens(it.headline).isNotEmpty() }
                .forEach { c ->
                    // 방안1 적용 후 headline 은 highlight 없이 wrapText → 줄당 drawString 1회로 그려진다.
                    val lines = CardImageGeneratorUtils.wrapText(probe, c.headline, font, contentWidth)
                    (listOf(c.headline) + lines).distinct().forEach { s ->
                        val gap = maxInternalBlankGap(s, font)
                        if (gap > gapThreshold) dropped += "[${c.name}] ${gap}px \"$s\""
                        println("  [${c.name}] 내부최대공백=${gap}px  ${if (gap > gapThreshold) "← 누락 의심" else "ok"}  | \"$s\"")
                    }
                }
            probe.dispose()

            withClue("번들 폰트로도 headline drawString 에서 글리프가 누락됨: $dropped") {
                dropped shouldBe emptyList()
            }
        }
    })

/** drawHighlightedText 처럼 line 을 highlight 조각 경계로 쪼갠 뒤, 조각별 stringWidth 합을 구한다. */
private fun fragmentedWidth(
    fm: java.awt.FontMetrics,
    line: String,
    frags: List<String>,
): Int {
    if (frags.isEmpty()) return fm.stringWidth(line)
    var remaining = line
    var total = 0
    while (remaining.isNotEmpty()) {
        val hit = frags.firstOrNull { it.isNotEmpty() && remaining.startsWith(it) }
        if (hit != null) {
            total += fm.stringWidth(hit)
            remaining = remaining.substring(hit.length)
            continue
        }
        val nextPos =
            frags
                .filter { it.isNotEmpty() }
                .mapNotNull { f -> remaining.indexOf(f).takeIf { it >= 0 } }
                .minOrNull() ?: remaining.length
        total += fm.stringWidth(remaining.substring(0, nextPos))
        remaining = remaining.substring(nextPos)
    }
    return total
}

/** drawMultilineHighlightedText 의 activeHighlights 갱신 로직을 재현(진단용). */
private fun nextActiveHighlights(
    line: String,
    highlightTexts: List<String>,
    active: Set<String>,
): Set<String> =
    highlightTexts
        .filter { h ->
            h.isNotEmpty() &&
                !line.contains(h) &&
                (highlightPrefix(line, h) != null || (h in active && h.contains(line)))
        }.toSet()

private fun highlightPrefix(
    line: String,
    highlight: String,
): String? {
    for (i in highlight.length - 1 downTo 1) {
        val prefix = highlight.substring(0, i)
        if (line.endsWith(prefix)) return prefix
    }
    return null
}

/** 공백으로 분리된 토큰 중 "한글에 붙은 ASCII(숫자/영문/구두점)" 또는 순수 숫자 토큰만 추출. */
private fun asciiInHangulTokens(text: String): List<String> {
    fun hasHangul(s: String) = s.any { it in '가'..'힣' }

    fun hasAsciiMark(s: String) = s.any { (it.code < 128 && it.isLetterOrDigit()) || it in "%,.()" }
    return text
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filter { tok ->
            (hasHangul(tok) && hasAsciiMark(tok)) || tok.matches(Regex("[0-9][0-9,.%]*"))
        }.distinct()
}

/**
 * 문자열을 주어진 폰트로 흰 배경에 drawString 1회로 그린 뒤,
 * 잉크가 있는 첫/마지막 열 사이에서 완전히 빈(배경만 있는) 열이 연속으로 이어지는 최대 길이(px).
 * 폰트가 정상이면 자간/어절공백 수준이지만, 글리프가 통째로 누락되면 큰 값이 된다.
 */
private fun maxInternalBlankGap(
    text: String,
    font: java.awt.Font,
): Int {
    val w = 2400
    val h = 220
    val img = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()
    CardImageGeneratorUtils.setupGraphics(g)
    g.color = Color.WHITE
    g.fillRect(0, 0, w, h)
    g.color = Color.BLACK
    g.font = font
    g.drawString(text, 20, 150)
    g.dispose()

    val inked =
        BooleanArray(w) { x ->
            (0 until h).any { y -> img.getRGB(x, y) and 0xFFFFFF != 0xFFFFFF }
        }
    val first = inked.indexOfFirst { it }
    val last = inked.indexOfLast { it }
    if (first < 0 || last <= first) return 0

    var maxGap = 0
    var cur = 0
    for (x in first..last) {
        if (inked[x]) {
            cur = 0
        } else {
            cur++
            if (cur > maxGap) maxGap = cur
        }
    }
    return maxGap
}