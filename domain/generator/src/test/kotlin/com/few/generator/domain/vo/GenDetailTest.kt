package com.few.generator.domain.vo

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class GenDetailTest :
    DescribeSpec({

        describe("GenDetail") {

            it("값에 의한 동등성 비교") {
                val genDetail1 =
                    GenDetail(
                        headline = "같은 헤드라인",
                        keywords = "같은 키워드",
                    )

                val genDetail2 =
                    GenDetail(
                        headline = "같은 헤드라인",
                        keywords = "같은 키워드",
                    )

                val genDetail3 =
                    GenDetail(
                        headline = "다른 헤드라인",
                        keywords = "다른 키워드",
                    )

                genDetail1 shouldBe genDetail2
                genDetail1.hashCode() shouldBe genDetail2.hashCode()
                genDetail1 shouldNotBe genDetail3
            }

            it("copy 메서드") {
                val original =
                    GenDetail(
                        headline = "원본 헤드라인",
                        keywords = "원본 키워드",
                    )

                val copied = original.copy(headline = "변경된 헤드라인")

                copied.headline shouldBe "변경된 헤드라인"
                copied.keywords shouldBe "원본 키워드"
                copied shouldNotBe original
            }

            it("구조 분해 선언") {
                val genDetail =
                    GenDetail(
                        headline = "구조 분해 헤드라인",
                        keywords = "구조 분해 키워드",
                    )

                val (headline, keywords) = genDetail

                headline shouldBe "구조 분해 헤드라인"
                keywords shouldBe "구조 분해 키워드"
            }
        }
    })