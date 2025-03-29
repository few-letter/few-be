package com.few.generator.controller

import com.few.generator.controller.request.WebContentsGeneratorRequest
import com.few.generator.controller.response.*
import com.few.generator.usecase.CreateAllUseCase
import com.few.generator.usecase.CreateProvisioningUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
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
    private val createAllUseCase: CreateAllUseCase,
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
    private val createProvisioningUseCase: CreateProvisioningUseCase,
) {
    @PostMapping(
        value = ["/contents"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createAll(
        @RequestBody request: WebContentsGeneratorRequest,
    ): ApiResponse<ApiResponse.SuccessBody<ContentsGeneratorResponse>> {
        val useCaseOut = createAllUseCase.execute(request.sourceUrl)

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

    @PostMapping(
        value = ["/contents/provisioning"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createProvisioning(
        @RequestBody request: WebContentsGeneratorRequest,
    ): ApiResponse<ApiResponse.SuccessBody<ContentsGeneratorResponse>> {
        val useCaseOut = createProvisioningUseCase.execute(request.sourceUrl)

        return ApiResponseGenerator.success(
            ContentsGeneratorResponse(
                sourceUrl = useCaseOut.sourceUrl,
                rawContentId = useCaseOut.rawContentId,
                provisioningContentId = useCaseOut.provisioningContentId,
            ),
            HttpStatus.CREATED,
        )
    }

    @GetMapping(value = ["/rawcontents/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
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
                        category = useCaseOut.provisioningContents.category,
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
                            type = it.type,
                            createdAt = it.createdAt!!,
                        )
                    },
            ),
            HttpStatus.OK,
        )
    }
}