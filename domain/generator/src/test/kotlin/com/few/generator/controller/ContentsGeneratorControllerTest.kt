package com.few.generator.controller

import com.few.generator.domain.Category
import com.few.generator.fixture.usecase.BrowseContentsFixture
import com.few.generator.fixture.usecase.BrowseDetailFixture
import com.few.generator.fixture.usecase.BrowseGroupGenResponsesFixture
import com.few.generator.service.GroupGenService
import com.few.generator.usecase.BrowseContentsUseCase
import com.few.generator.usecase.GroupGenBrowseUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
import com.few.generator.usecase.SchedulingUseCase
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate
import org.springframework.http.MediaType as SpringMediaType

class ContentsGeneratorControllerTest :
    DescribeSpec({

        val schedulingUseCase = mockk<SchedulingUseCase>()
        val rawContentsBrowseContentUseCase = mockk<RawContentsBrowseContentUseCase>()
        val browseContentsUseCase = mockk<BrowseContentsUseCase>()
        val groupGenService = mockk<GroupGenService>()
        val groupGenBrowseUseCase = mockk<GroupGenBrowseUseCase>()

        val controller =
            ContentsGeneratorController(
                schedulingUseCase,
                rawContentsBrowseContentUseCase,
                browseContentsUseCase,
                groupGenService,
                groupGenBrowseUseCase,
            )

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        describe("ContentsGeneratorController") {

            describe("POST /api/v1/contents/schedule") {
                it("콘텐츠 스케줄링을 실행한다") {
                    every { schedulingUseCase.execute() } returns Unit

                    mockMvc
                        .perform(
                            post("/api/v1/contents/schedule")
                                .contentType(SpringMediaType.APPLICATION_JSON),
                        ).andExpect(status().isOk)

                    verify { schedulingUseCase.execute() }
                }
            }

            describe("POST /api/v1/contents/groups/schedule") {
                it("그룹 콘텐츠 스케줄링을 실행한다") {
                    every { groupGenService.createAllGroupGen() } returns emptyList()

                    mockMvc
                        .perform(
                            post("/api/v1/contents/groups/schedule")
                                .contentType(SpringMediaType.APPLICATION_JSON),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))

                    verify { groupGenService.createAllGroupGen() }
                }
            }

            describe("GET /api/v1/contents") {
                it("기본 파라미터로 콘텐츠 목록을 조회한다") {
                    val expectedInput = BrowseContentsUseCaseIn(-1L, null)
                    val useCaseOutput = BrowseContentsFixture.default().sample()
                    every { browseContentsUseCase.execute(expectedInput) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents")
                                .param("prevContentId", "-1"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data.contents").isNotEmpty)
                        .andExpect(jsonPath("$.data.contents[0].id").value(1))
                        .andExpect(jsonPath("$.data.contents[0].headline").value("테스트 헤드라인"))
                        .andExpect(jsonPath("$.data.contents[0].summary").value("테스트 요약"))
                        .andExpect(jsonPath("$.data.contents[0].highlightTexts[0]").value("하이라이트1"))
                        .andExpect(jsonPath("$.data.contents[0].highlightTexts[1]").value("하이라이트2"))
                        .andExpect(jsonPath("$.data.contents[0].mediaType.code").value(1))
                        .andExpect(
                            jsonPath("$.data.contents[0].mediaType.value").value("조선일보"),
                        ).andExpect(
                            jsonPath("$.data.contents[0].category.code").value(2),
                        ).andExpect(
                            jsonPath("$.data.contents[0].category.value").value("기술"),
                        ).andExpect(jsonPath("$.data.isLast").value(false))

                    verify { browseContentsUseCase.execute(expectedInput) }
                }

                it("카테고리 파라미터와 함께 콘텐츠 목록을 조회한다") {
                    val expectedInput = BrowseContentsUseCaseIn(5L, 2)
                    val useCaseOutput = BrowseContentsFixture.empty().sample()
                    every { browseContentsUseCase.execute(expectedInput) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents")
                                .param("prevContentId", "5")
                                .param("category", "2"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data.contents").isEmpty)
                        .andExpect(jsonPath("$.data.isLast").value(true))

                    verify { browseContentsUseCase.execute(expectedInput) }
                }

                it("여러 콘텐츠가 있을 때 올바르게 반환한다") {
                    val expectedInput = BrowseContentsUseCaseIn(-1L, null)
                    val useCaseOutput = BrowseContentsFixture.multiple().sample()
                    every { browseContentsUseCase.execute(expectedInput) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents")
                                .param("prevContentId", "-1"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.data.contents.length()").value(2))
                        .andExpect(jsonPath("$.data.contents[0].headline").value("첫 번째 뉴스"))
                        .andExpect(jsonPath("$.data.contents[1].headline").value("두 번째 뉴스"))
                        .andExpect(
                            jsonPath("$.data.contents[0].mediaType.value").value("조선일보"),
                        ).andExpect(
                            jsonPath("$.data.contents[1].mediaType.value").value("경향신문"),
                        ).andExpect(
                            jsonPath("$.data.contents[0].category.value").value("기술"),
                        ).andExpect(jsonPath("$.data.contents[1].category.value").value("생활"))
                        .andExpect(jsonPath("$.data.isLast").value(false))

                    verify { browseContentsUseCase.execute(expectedInput) }
                }
            }

            describe("GET /api/v1/contents/{id}") {
                it("특정 ID로 상세 콘텐츠를 조회한다") {
                    val contentId = 1L
                    val useCaseOutput = BrowseDetailFixture.giveMeDefault().build()
                    every { rawContentsBrowseContentUseCase.execute(contentId) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents/{id}", contentId),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data.rawContents.id").value(201))
                        .andExpect(jsonPath("$.data.rawContents.title").value("원본 제목"))
                        .andExpect(jsonPath("$.data.provisioningContents.id").value(101))
                        .andExpect(jsonPath("$.data.gen.id").value(1))
                        .andExpect(jsonPath("$.data.gen.headline").value("생성된 헤드라인"))

                    verify { rawContentsBrowseContentUseCase.execute(contentId) }
                }
            }

            describe("GET /api/v1/contents/categories") {
                it("모든 카테고리 목록을 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v1/contents/categories"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data").isArray)
                        .andExpect(jsonPath("$.data.length()").value(Category.entries.size))
                        .andExpect(
                            jsonPath("$.data[?(@.value == '기술')].code").value(2),
                        ).andExpect(jsonPath("$.data[?(@.value == '생활')].code").value(4))
                        .andExpect(
                            jsonPath("$.data[?(@.value == '정치')].code").value(8),
                        ).andExpect(jsonPath("$.data[?(@.value == '경제')].code").value(16))
                        .andExpect(jsonPath("$.data[?(@.value == '사회')].code").value(32))
                        .andExpect(jsonPath("$.data[?(@.value == '기타')].code").value(0))
                }
            }

            describe("GET /api/v1/contents/groups") {
                it("기본 파라미터로 그룹 콘텐츠 목록을 조회한다") {
                    val useCaseOutput = BrowseGroupGenResponsesFixture.default().sample()
                    every { groupGenBrowseUseCase.execute(null) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents/groups"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data.groups").isNotEmpty)
                        .andExpect(jsonPath("$.data.groups[0].id").value(1))
                        .andExpect(jsonPath("$.data.groups[0].category").value(2))
                        .andExpect(jsonPath("$.data.groups[0].headline").value("그룹 헤드라인"))
                        .andExpect(jsonPath("$.data.groups[0].summary").value("그룹 요약"))
                        .andExpect(jsonPath("$.data.groups[0].highlightTexts[0]").value("그룹 하이라이트1"))
                        .andExpect(jsonPath("$.data.groups[0].highlightTexts[1]").value("그룹 하이라이트2"))
                        .andExpect(jsonPath("$.data.groups[0].groupSourceHeadlines").isArray)
                        .andExpect(jsonPath("$.data.groups[0].groupSourceHeadlines[0].headline").value("소스 헤드라인1"))
                        .andExpect(jsonPath("$.data.groups[0].groupSourceHeadlines[0].url").value("https://example.com/1"))

                    verify { groupGenBrowseUseCase.execute(null) }
                }

                it("날짜 파라미터와 함께 그룹 콘텐츠 목록을 조회한다") {
                    val date = LocalDate.of(2023, 5, 15)
                    val useCaseOutput = BrowseGroupGenResponsesFixture.empty().sample()
                    every { groupGenBrowseUseCase.execute(date) } returns useCaseOutput

                    mockMvc
                        .perform(
                            get("/api/v1/contents/groups")
                                .param("date", "2023-05-15"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.message").value("성공"))
                        .andExpect(jsonPath("$.data.groups").isEmpty)

                    verify { groupGenBrowseUseCase.execute(date) }
                }
            }
        }
    })