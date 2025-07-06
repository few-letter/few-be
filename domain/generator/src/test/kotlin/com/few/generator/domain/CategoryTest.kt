package com.few.generator.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import web.handler.exception.BadRequestException

class CategoryTest :
    DescribeSpec({

        describe("Category") {

            describe("기본 속성 검증") {
                it("각 카테고리는 고유한 비트 플래그 코드와 제목을 가진다") {
                    Category.TECHNOLOGY.code shouldBe 2
                    Category.TECHNOLOGY.title shouldBe "기술"

                    Category.LIFE.code shouldBe 4
                    Category.LIFE.title shouldBe "생활"

                    // 비트 연산으로 조합 및 확인 가능
                    val combined = Category.TECHNOLOGY.code or Category.LIFE.code
                    ((combined and Category.TECHNOLOGY.code) != 0) shouldBe true
                    ((combined and Category.POLITICS.code) != 0) shouldBe false
                }
            }

            describe("from(code) 메서드") {
                withData(
                    nameFn = { "code ${it.first} -> ${it.second}" },
                    2 to Category.TECHNOLOGY,
                    4 to Category.LIFE,
                    8 to Category.POLITICS,
                    16 to Category.ECONOMY,
                    32 to Category.SOCIETY,
                    0 to Category.ETC,
                ) { (code, expected) ->
                    Category.from(code) shouldBe expected
                }

                it("유효하지 않은 코드로 예외를 발생시킨다") {
                    val exception =
                        shouldThrow<BadRequestException> {
                            Category.from(999)
                        }
                    exception.message shouldBe "Invalid Category code: 999"
                }
            }

            describe("from(title) 메서드") {
                withData(
                    nameFn = { "title '${it.first}' -> ${it.second}" },
                    "기술" to Category.TECHNOLOGY,
                    "생활" to Category.LIFE,
                    "정치" to Category.POLITICS,
                    "경제" to Category.ECONOMY,
                    "사회" to Category.SOCIETY,
                    "기타" to Category.ETC,
                ) { (title, expected) ->
                    Category.from(title) shouldBe expected
                }

                it("유효하지 않은 제목으로 예외를 발생시킨다") {
                    val exception =
                        shouldThrow<BadRequestException> {
                            Category.from("존재하지 않는 카테고리")
                        }
                    exception.message shouldBe "Invalid Category title: 존재하지 않는 카테고리"
                }
            }

            describe("groupGenEntries 메서드") {
                it("ETC 카테고리를 제외한 모든 카테고리를 반환한다") {
                    val groupGenCategories = Category.groupGenEntries()
                    val expected = Category.entries.filter { it != Category.ETC }

                    groupGenCategories shouldContainExactlyInAnyOrder expected
                }
            }
        }
    })