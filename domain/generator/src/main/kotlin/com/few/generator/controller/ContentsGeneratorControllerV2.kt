package com.few.generator.controller

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.controller.response.*
import com.few.generator.usecase.BrowseContentsUseCase
import com.few.generator.usecase.GroupGenBrowseUseCase
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import com.few.generator.usecase.input.BrowseGroupGenUseCaseIn
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Validated
@RestController
@RequestMapping("/api/v2")
class ContentsGeneratorControllerV2(
    private val browseContentsUseCase: BrowseContentsUseCase,
    private val groupGenBrowseUseCase: GroupGenBrowseUseCase,
) {
    @GetMapping(
        value = [
            "/contents/local-news",
            "/contents/global-news",
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
        val url =
            ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .toUriString()
        val region = if (url.contains("global-news")) Region.GLOBAL else Region.LOCAL
        val category = categoryCode?.let { Category.from(it) }
        val ucOuts = browseContentsUseCase.execute(BrowseContentsUseCaseIn(prevGenId, category, region))

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

    @GetMapping(
        value = [
            "/contents/local-news/categories",
            "/contents/global-news/categories",
        ],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getCategories(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> {
        val url =
            ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .toUriString()

        return if (url.contains("global-news")) {
            ApiResponseGenerator.success(
                Category.entries
                    .filter {
                        it == Category.ECONOMY || it == Category.TECHNOLOGY || it == Category.POLITICS
                    }.map { CodeValueResponse(code = it.code, value = it.title) },
                HttpStatus.OK,
            )
        } else {
            ApiResponseGenerator.success(
                Category.entries.map { CodeValueResponse(code = it.code, value = it.title) },
                HttpStatus.OK,
            )
        }
    }

    @GetMapping(
        value = [
            "/contents/local-news/groups",
            "/contents/global-news/groups",
        ],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getGroupGens(): ApiResponse<ApiResponse.SuccessBody<BrowseGroupGenResponses>> {
        val url =
            ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .toUriString()
        val region = if (url.contains("global-news")) Region.GLOBAL else Region.LOCAL
        val response = groupGenBrowseUseCase.execute(BrowseGroupGenUseCaseIn(region))
        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }
}