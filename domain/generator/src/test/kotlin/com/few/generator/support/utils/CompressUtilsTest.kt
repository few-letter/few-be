package com.few.generator.support.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.util.*
import java.util.zip.ZipException

class CompressUtilsTest :
    DescribeSpec({

        describe("CompressUtils") {

            describe("압축 및 해제") {
                withData(
                    nameFn = { "입력: ${it.take(20)}..." },
                    "Hello World! This is a test string for compression.",
                    "안녕하세요! 이것은 한글 압축 테스트입니다. 가나다라마바사아자차카타파하",
                    "Hello! @#$%^&*()_+-=[]{}|;':\",.<>?",
                    """{"name": "테스트", "value": 123, "array": [1, 2, 3]}""",
                    "A".repeat(1000),
                ) { original ->
                    val compressed = CompressUtils.compress(original)
                    val decompressed = CompressUtils.decompress(compressed)

                    decompressed shouldBe original
                    compressed shouldNotBe original
                    compressed.shouldNotBeEmpty()
                }

                it("빈 문자열을 압축하고 해제할 수 있다") {
                    val original = ""

                    val compressed = CompressUtils.compress(original)
                    val decompressed = CompressUtils.decompress(compressed)

                    decompressed shouldBe original
                }
            }

            describe("오류 처리") {
                it("잘못된 Base64 문자열로 해제 시도 시 예외가 발생한다") {
                    val invalidBase64 = "This is not a valid Base64 string!"

                    shouldThrow<IllegalArgumentException> {
                        CompressUtils.decompress(invalidBase64)
                    }
                }

                it("잘못된 GZIP 데이터로 해제 시도 시 예외가 발생한다") {
                    val invalidGzip = Base64.getEncoder().encodeToString("invalid gzip data".toByteArray())

                    shouldThrow<ZipException> {
                        CompressUtils.decompress(invalidGzip)
                    }
                }
            }

            describe("압축 효율성") {
                it("반복되는 패턴이 많은 문자열은 압축 효율이 좋다") {
                    val original = "AAAAAAAAAA".repeat(100)

                    val compressed = CompressUtils.compress(original)

                    compressed.length shouldBeLessThan (original.length / 2)
                }

                it("압축된 문자열은 유효한 Base64 형태이다") {
                    val original = "Test string for Base64 validation"

                    val compressed = CompressUtils.compress(original)

                    // Base64 디코딩이 성공하는지 확인하여 유효성 검증
                    val decoded = Base64.getDecoder().decode(compressed)
                    decoded.isNotEmpty() shouldBe true
                }
            }
        }
    })