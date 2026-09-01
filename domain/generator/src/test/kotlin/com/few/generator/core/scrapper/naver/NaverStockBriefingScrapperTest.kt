package com.few.generator.core.scrapper.naver

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.mockk.mockk

class NaverStockBriefingScrapperTest :
    FunSpec({
        val scrapper = NaverStockBriefingScrapper(mockk(relaxed = true))

        fun wrap(inner: String): String =
            """
            <html><body>
              <div id="content"><div><article>
                <div class="ContentText_area-content__JVudc">$inner</div>
              </article></div></div>
            </body></html>
            """.trimIndent()

        test("<b> 제목과 BriefingSection_paragraph 본문을 한 쌍으로 묶는다") {
            val html =
                wrap(
                    """
                    <b>💻 반도체 수출 기록이 대형주 버팀목</b>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">8월 반도체 수출이 역대 최대를 기록했어요.</p>
                    </div>
                    <b>📈 코스피는 버텼지만 코스닥은 약세</b>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">코스피는 보합권, 코스닥은 1% 넘게 하락했어요.</p>
                    </div>
                    """.trimIndent(),
                )

            val result = scrapper.parseContent(html)

            result shouldHaveSize 2
            result[0] shouldBe
                StockBriefingRawContent("💻 반도체 수출 기록이 대형주 버팀목", "8월 반도체 수출이 역대 최대를 기록했어요.")
            result[1] shouldBe
                StockBriefingRawContent("📈 코스피는 버텼지만 코스닥은 약세", "코스피는 보합권, 코스닥은 1% 넘게 하락했어요.")
        }

        test("지수/종목 카드(VisualCard)의 <p> 는 본문으로 수집하지 않는다") {
            val html =
                wrap(
                    """
                    <b>💻 반도체 수출 기록이 대형주 버팀목</b>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">삼성전자와 SK하이닉스가 시장을 받쳐줬어요.</p>
                    </div>
                    <article class="VisualCard_card__J5uDI">
                      <ul class="IndexCard_list__c9FVq">
                        <li><a><p class="IndexCard_name__9aEan">코스피</p><p class="IndexCard_current__woCDb">6,835.80</p></a></li>
                      </ul>
                    </article>
                    <article class="VisualCard_card__J5uDI">
                      <ul class="GeneralStockCard_list__jxgDa">
                        <li><a><p class="GeneralStockCard_name__d_H7v">삼성전자</p><p class="GeneralStockCard_price__Brr0C">261,000</p></a></li>
                      </ul>
                    </article>
                    """.trimIndent(),
                )

            val result = scrapper.parseContent(html)

            result shouldHaveSize 1
            result[0] shouldBe
                StockBriefingRawContent("💻 반도체 수출 기록이 대형주 버팀목", "삼성전자와 SK하이닉스가 시장을 받쳐줬어요.")
        }

        test("본문 내 출처 뱃지 버튼 텍스트는 제거한다") {
            val html =
                wrap(
                    """
                    <b>🔋 2차전지</b>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">SK이노베이션이 올랐어요.<button type="button" class="ContentText_badge__y0sJi" aria-label="출처 2번 보기">2</button> 다만 LG에너지솔루션은 하락했어요.</p>
                    </div>
                    """.trimIndent(),
                )

            val result = scrapper.parseContent(html)

            result shouldHaveSize 1
            result[0].body shouldBe "SK이노베이션이 올랐어요. 다만 LG에너지솔루션은 하락했어요."
            result[0].body shouldNotContain "2"
        }

        test("같은 제목 아래 여러 문단이 있으면 공백으로 이어붙인다") {
            val html =
                wrap(
                    """
                    <b>🌏 아시아 증시</b>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">대만 가권은 강세로 마감했어요.</p>
                    </div>
                    <div class="BriefingSection_area-paragraph__K9OHd">
                      <p class="BriefingSection_paragraph__cmBpR">일본과 중국 본토는 약보합이었어요.</p>
                    </div>
                    """.trimIndent(),
                )

            val result = scrapper.parseContent(html)

            result shouldHaveSize 1
            result[0].body shouldBe "대만 가권은 강세로 마감했어요. 일본과 중국 본토는 약보합이었어요."
        }

        test("CONTENT_SELECTOR 영역이 없으면 빈 리스트를 반환한다") {
            scrapper.parseContent("<html><body><div>no content</div></body></html>") shouldBe emptyList()
        }
    })