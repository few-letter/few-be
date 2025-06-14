package com.few.generator.controller

import com.few.generator.controller.request.CreateGensRequest
import com.few.generator.controller.request.WebContentsGeneratorRequest
import com.few.generator.controller.response.*
import com.few.generator.domain.Category
import com.few.generator.domain.GenType
import com.few.generator.usecase.CreateAllUseCase
import com.few.generator.usecase.CreateGenUseCase
import com.few.generator.usecase.CreateProvisioningUseCase
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
    private val createAllUseCase: CreateAllUseCase,
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
    private val createProvisioningUseCase: CreateProvisioningUseCase,
    private val createGenUseCase: CreateGenUseCase,
) {
    @PostMapping(
        value = ["/contents"],
    )
    fun createAll(): ApiResponse<ApiResponse.Success> {
        schedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
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

    @PostMapping(
        value = ["/contents/gens"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createGens(
        @RequestBody request: CreateGensRequest,
    ): ApiResponse<ApiResponse.SuccessBody<ContentsGeneratorResponse>> {
        val useCaseOut = createGenUseCase.execute(request.provContentsId, request.types)

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
                        category =
                            CodeValueResponse(
                                code = useCaseOut.provisioningContents.category.code,
                                value = useCaseOut.provisioningContents.category.value,
                            ),
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
                            type =
                                CodeValueResponse(
                                    code = it.type.code,
                                    value = it.type.value,
                                ),
                            createdAt = it.createdAt!!,
                        )
                    },
            ),
            HttpStatus.OK,
        )
    }

    @GetMapping(value = ["/contents/provisioning/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProvisioningCategories(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> =
        ApiResponseGenerator.success(
            Category.values().map {
                CodeValueResponse(code = it.code, value = it.title)
            },
            HttpStatus.OK,
        )

    @GetMapping(value = ["/contents/gens/types"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGenTypes(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> =
        ApiResponseGenerator.success(
            GenType.values().map {
                CodeValueResponse(code = it.code, value = it.title)
            },
            HttpStatus.OK,
        )
}