package com.few.generator.controller

import com.few.generator.controller.response.*
import com.few.generator.domain.Category
import com.few.generator.usecase.BrowseContentsUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
import com.few.generator.usecase.SchedulingUseCase
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import web.ApiResponse
import web.ApiResponseGenerator

@Validated
@RestController
@RequestMapping("/api/v1")
class ContentsGeneratorController(
    private val schedulingUseCase: SchedulingUseCase,
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
    private val browseContentsUseCase: BrowseContentsUseCase,
) {
    @PostMapping(
        value = ["/contents/scheduling"],
    )
    fun createAll(): ApiResponse<ApiResponse.Success> {
        schedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @GetMapping(
        value = ["/contents"],
    )
    fun readContents(
        @RequestParam(
            required = false,
            defaultValue = "-1",
        ) prevContentId: Long,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseContentResponses>> {
        val ucOuts = browseContentsUseCase.execute(prevContentId)

        val response =
            BrowseContentResponses(
                contents =
                    ucOuts.contents.map {
                        BrowseContentResponse(
                            id = it.id,
                            url = it.url,
                            thumbnailImageUrl = it.thumbnailImageUrl,
                            mediaType = CodeValueResponse(code = it.mediaType.code, value = it.mediaType.title),
                            headline = it.headline,
                            summary = it.summary,
                            highlightTexts = it.highlightTexts,
                            createdAt = it.createdAt,
                            category = CodeValueResponse(code = it.category.code, value = it.category.title),
                        )
                    },
                isLast = ucOuts.isLast,
            )

        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }

    @GetMapping(value = ["/contents/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByRawContents(
        @PathVariable(value = "id")
        @Min(value = 1, message = "{min.id}")
        genId: Long,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseContentsResponse>> {
        val useCaseOut = rawContentsBrowseContentUseCase.execute(genId)

        return ApiResponseGenerator.success(
            BrowseContentsResponse(
                rawContents =
                    BrowseRawContentsResponse(
                        id = useCaseOut.rawContents.id!!,
                        url = useCaseOut.rawContents.url,
                        title = useCaseOut.rawContents.title,
                        description = useCaseOut.rawContents.description,
                        thumbnailImageUrl = useCaseOut.rawContents.thumbnailImageUrl,
                        rawTexts = useCaseOut.rawContents.rawTexts,
                        imageUrls = useCaseOut.rawContents.imageUrls,
                        mediaType =
                            CodeValueResponse(
                                code = useCaseOut.rawContents.mediaType.code,
                                value = useCaseOut.rawContents.mediaType.title,
                            ),
                        createdAt = useCaseOut.rawContents.createdAt,
                    ),
                provisioningContents =
                    BrowseProvisioningContentsResponse(
                        id = useCaseOut.provisioningContents.id,
                        rawContentsId = useCaseOut.provisioningContents.rawContentsId,
                        completionIds = useCaseOut.provisioningContents.completionIds,
                        bodyTextsJson = useCaseOut.provisioningContents.bodyTextsJson,
                        coreTextsJson = useCaseOut.provisioningContents.coreTextsJson,
                        createdAt = useCaseOut.provisioningContents.createdAt,
                    ),
                gen =
                    BrowseGenResponse(
                        id = useCaseOut.gen.id,
                        provisioningContentsId = useCaseOut.gen.provisioningContentsId,
                        completionIds = useCaseOut.gen.completionIds,
                        headline = useCaseOut.gen.headline,
                        summary = useCaseOut.gen.summary,
                        highlightTexts = useCaseOut.gen.highlightTexts,
                        category =
                            CodeValueResponse(
                                code = useCaseOut.gen.category.code,
                                value = useCaseOut.gen.category.title,
                            ),
                        createdAt = useCaseOut.gen.createdAt,
                    ),
            ),
            HttpStatus.OK,
        )
    }

    @GetMapping(value = ["/contents/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProvisioningCategories(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> =
        ApiResponseGenerator.success(
            Category.entries.map {
                CodeValueResponse(code = it.code, value = it.title)
            },
            HttpStatus.OK,
        )
}