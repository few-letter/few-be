package com.few.generator.domain

import com.few.generator.fixture.TestConstants
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import web.handler.exception.BadRequestException

class MediaTypeTest :
    DescribeSpec({

        describe("MediaType") {

            describe(TestConstants.MEDIATYPE_TEST_BASIC_PROPERTIES_VERIFICATION) {
                it(TestConstants.MEDIATYPE_TEST_UNIQUE_CODE_TITLE_KEYWORDS) {
                    MediaType.CHOSUN.code shouldBe 1
                    MediaType.CHOSUN.title shouldBe TestConstants.MEDIATYPE_TITLE_CHOSUN
                    MediaType.CHOSUN.keywords shouldContain TestConstants.MEDIATYPE_KEYWORD_CHOSUN

                    MediaType.KHAN.code shouldBe 2
                    MediaType.KHAN.title shouldBe TestConstants.MEDIATYPE_TITLE_KHAN
                    MediaType.KHAN.keywords shouldContain TestConstants.MEDIATYPE_KEYWORD_KHAN
                }

                it(TestConstants.MEDIATYPE_TEST_ALL_MEDIA_TYPES_HAVE_KEYWORDS) {
                    MediaType.entries.forEach { mediaType ->
                        if (mediaType != MediaType.ETC) {
                            mediaType.keywords.shouldNotBeEmpty()
                        }
                    }
                }
            }

            describe(TestConstants.MEDIATYPE_TEST_FROM_CODE_METHOD) {
                withData(
                    nameFn = { "code ${it.first} -> ${it.second}" },
                    1 to MediaType.CHOSUN,
                    2 to MediaType.KHAN,
                    3 to MediaType.SBS,
                    0 to MediaType.ETC,
                ) { (code, expected) ->
                    MediaType.from(code) shouldBe expected
                }

                it(TestConstants.MEDIATYPE_TEST_INVALID_CODE_THROWS_EXCEPTION) {
                    val exception =
                        shouldThrow<BadRequestException> {
                            MediaType.from(999)
                        }
                    exception.message shouldBe String.format(TestConstants.MEDIATYPE_TEST_INVALID_CODE_MESSAGE_FORMAT, 999)
                }
            }

            describe(TestConstants.MEDIATYPE_TEST_FROM_TITLE_METHOD) {
                withData(
                    nameFn = { "title '${it.first}' -> ${it.second}" },
                    TestConstants.MEDIATYPE_TITLE_CHOSUN to MediaType.CHOSUN,
                    TestConstants.MEDIATYPE_TITLE_KHAN to MediaType.KHAN,
                    TestConstants.MEDIATYPE_TITLE_SBS to MediaType.SBS,
                    TestConstants.MEDIATYPE_TITLE_ETC to MediaType.ETC,
                ) { (title, expected) ->
                    MediaType.from(title) shouldBe expected
                }

                it(TestConstants.MEDIATYPE_TEST_INVALID_TITLE_THROWS_EXCEPTION) {
                    val exception =
                        shouldThrow<BadRequestException> {
                            MediaType.from(TestConstants.MEDIATYPE_INVALID_TITLE)
                        }
                    exception.message shouldBe
                        String.format(TestConstants.MEDIATYPE_TEST_INVALID_NAME_MESSAGE_FORMAT, TestConstants.MEDIATYPE_INVALID_TITLE)
                }
            }

            describe(TestConstants.MEDIATYPE_TEST_FIND_METHOD) {
                withData(
                    nameFn = { "input '${it.first.take(30)}...' -> ${it.second}" },
                    TestConstants.MEDIATYPE_URL_CHOSUN to MediaType.CHOSUN,
                    TestConstants.MEDIATYPE_URL_KHAN to MediaType.KHAN,
                    TestConstants.MEDIATYPE_URL_SBS to MediaType.SBS,
                    TestConstants.MEDIATYPE_URL_YONHAPNEWS_TV to MediaType.YONHAPNEWS_TV,
                    TestConstants.MEDIATYPE_KEYWORD_CHOSUN.uppercase() to MediaType.CHOSUN, // 대소문자
                    TestConstants.MEDIATYPE_PARTIAL_MATCH_CHOSUN to MediaType.CHOSUN, // 부분 문자열
                    TestConstants.MEDIATYPE_UNKNOWN_NEWS_SITE to MediaType.ETC, // 매칭 실패
                    TestConstants.MEDIATYPE_EMPTY_STRING to MediaType.ETC, // 빈 문자열
                ) { (input, expected) ->
                    MediaType.find(input) shouldBe expected
                }
            }
        }
    })