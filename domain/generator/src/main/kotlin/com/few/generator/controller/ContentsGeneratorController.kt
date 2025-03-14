package com.few.generator.controller

import com.few.generator.controller.request.WebContentsGeneratorRequest
import com.few.generator.controller.response.*
import com.few.generator.usecase.ContentsGeneratorUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import web.ApiResponse
import web.ApiResponseGenerator

@RestController
@RequestMapping("/api/v1")
class ContentsGeneratorController(
    private val contentsGeneratorUseCase: ContentsGeneratorUseCase,
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
) {
    @PostMapping(
        value = ["/generators/contents"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun create(
        @RequestBody request: WebContentsGeneratorRequest,
    ): ApiResponse<ApiResponse.SuccessBody<ContentsGeneratorResponse>> {
        val useCaseOut = contentsGeneratorUseCase.execute(request.sourceUrl)

        return ApiResponseGenerator.success(
            ContentsGeneratorResponse(
                sourceUrl = useCaseOut.sourceUrl,
                rawContentId = useCaseOut.rawContentId,
                provisioningContentId = useCaseOut.provisioningContentId,
                genIds = useCaseOut.genIds,
            ),
            HttpStatus.CREATED,
        )
    }

    @GetMapping(value = ["/rawcontents/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByRawContents(
        @PathVariable(value = "id")
        @Min(value = 1, message = "{min.id}")
        rawContentsId: Long,
    ): ApiResponse<ApiResponse.SuccessBody<BrowseContentsResponse>> {
        val useCaseOut = rawContentsBrowseContentUseCase.execute(rawContentsId)

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
                        createdAt = useCaseOut.rawContents.createdAt!!,
                    ),
                provisioningContents =
                    BrowseProvisioningContentsResponse(
                        id = useCaseOut.provisioningContents.id!!,
                        rawContentsId = useCaseOut.provisioningContents.rawContentsId,
                        completionIds = useCaseOut.provisioningContents.completionIds,
                        bodyTextsJson = useCaseOut.provisioningContents.bodyTextsJson,
                        coreTextsJson = useCaseOut.provisioningContents.coreTextsJson,
                        createdAt = useCaseOut.provisioningContents.createdAt!!,
                    ),
                gens =
                    useCaseOut.gens.map {
                        BrowseGenResponse(
                            id = it.id!!,
                            provisioningContentsId = it.provisioningContentsId,
                            completionIds = it.completionIds,
                            headline = it.headline,
                            summary = it.summary,
                            highlightTexts = it.highlightTexts,
                            createdAt = it.createdAt!!,
                        )
                    },
            ),
            HttpStatus.OK,
        )
    }
}