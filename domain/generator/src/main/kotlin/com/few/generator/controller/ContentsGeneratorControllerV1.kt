package com.few.generator.controller

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.Region
import com.few.generator.controller.response.*
import com.few.generator.usecase.BrowseContentsUseCase
import com.few.generator.usecase.GroupGenBrowseUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import com.few.generator.usecase.input.BrowseGroupGenUseCaseIn
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Min
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/api/v1")
class ContentsGeneratorControllerV1(
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
    private val browseContentsUseCase: BrowseContentsUseCase,
    private val groupGenBrowseUseCase: GroupGenBrowseUseCase,
) {
    @Operation(
        summary = "사용 중단 예정 API",
        description =
            "이 엔드포인트는 다음 버전에서 제거될 예정입니다. " +
                "/api/v2/contents/local-news 및 /api/v2/contents/global-news 를 참고하세요",
        deprecated = true,
    )
    @GetMapping(
        value = [
            "/contents",
        ],
    )
    fun readNewsContents(
        @RequestParam(
            value = "prevContentId",
            required = false,
            defaultValue = "-1",
        )
        prevGenId: Long,
        @RequestParam(
            value = "category",
            required = false,
        )
        categoryCode: Int?,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseContentResponses>> {
        val category = categoryCode?.let { Category.from(it) }
        val ucOuts = browseContentsUseCase.execute(BrowseContentsUseCaseIn(prevGenId, category, Region.LOCAL))

        val response =
            BrowseContentResponses(
                contents =
                    ucOuts.contents.map {
                        BrowseContentResponse(
                            id = it.id,
                            url = it.url,
                            thumbnailImageUrl = it.thumbnailImageUrl,
                            mediaType =
                                CodeValueResponse(
                                    code = it.mediaType.code,
                                    value = it.mediaType.title,
                                ),
                            headline = it.headline,
                            summary = it.summary,
                            highlightTexts = it.highlightTexts,
                            createdAt = it.createdAt,
                            category =
                                CodeValueResponse(
                                    code = it.category.code,
                                    value = it.category.title,
                                ),
                        )
                    },
                isLast = ucOuts.isLast,
            )

        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }

    @GetMapping(value = ["/contents/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getContentsDetail(
        @PathVariable(value = "id")
        @Min(value = 1, message = "{min.id}")
        genId: Long,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseContentDetailResponse>> {
        val useCaseOut = rawContentsBrowseContentUseCase.execute(genId)

        return ApiResponseGenerator.success(
            BrowseContentDetailResponse(
                id = useCaseOut.gen.id,
                thumbnailImageUrl = useCaseOut.rawContents.thumbnailImageUrl,
                mediaType =
                    CodeValueResponse(
                        code = useCaseOut.rawContents.mediaType.code,
                        value = useCaseOut.rawContents.mediaType.title,
                    ),
                url = useCaseOut.rawContents.url,
                headline = useCaseOut.gen.headline,
                summary = useCaseOut.gen.summary,
                highlightTexts = useCaseOut.gen.highlightTexts,
                category =
                    CodeValueResponse(
                        code = useCaseOut.gen.category.code,
                        value = useCaseOut.gen.category.title,
                    ),
                region =
                    useCaseOut.gen.region?.let {
                        CodeValueResponse(
                            code = it.code,
                            value = it.name,
                        )
                    },
                createdAt = useCaseOut.gen.createdAt,
            ),
            HttpStatus.OK,
        )
    }

    @Operation(
        summary = "사용 중단 예정 API",
        description =
            "이 엔드포인트는 다음 버전에서 제거될 예정입니다. " +
                "/api/v2/contents/local-news/categories 및 /api/v2/contents/global-news/categories 를 참고하세요",
        deprecated = true,
    )
    @GetMapping(value = ["/contents/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCategories(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> =
        ApiResponseGenerator.success(
            Category.entries.map { CodeValueResponse(code = it.code, value = it.title) },
            HttpStatus.OK,
        )

    @Operation(
        summary = "사용 중단 예정 API",
        description =
            "이 엔드포인트는 다음 버전에서 제거될 예정입니다. " +
                "/api/v2/contents/local-news/groups 및 /api/v2/contents/global-news/groups 를 참고하세요",
        deprecated = true,
    )
    @GetMapping(value = ["/contents/groups"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGroupGens(
        @RequestParam(
            value = "date",
            required = false,
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseGroupGenResponses>> {
        val response =
            groupGenBrowseUseCase.execute(
                BrowseGroupGenUseCaseIn(date, Region.LOCAL),
            )
        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }

    @GetMapping(value = ["/contents/types"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getContentsTypes(): ApiResponse<ApiResponse.SuccessBody<List<String>>> =
        ApiResponseGenerator.success(
            ContentsType.entries.map { it.title },
            HttpStatus.OK,
        )
}