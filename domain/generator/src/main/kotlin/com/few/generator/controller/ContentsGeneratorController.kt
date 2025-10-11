package com.few.generator.controller

import com.few.common.domain.Category
import com.few.common.domain.ContentsType
import com.few.common.domain.Region
import com.few.common.exception.BadRequestException
import com.few.generator.controller.request.ContentsSchedulingRequest
import com.few.generator.controller.response.*
import com.few.generator.usecase.BrowseContentsUseCase
import com.few.generator.usecase.GenSchedulingUseCase
import com.few.generator.usecase.GlobalGenSchedulingUseCase
import com.few.generator.usecase.GroupGenBrowseUseCase
import com.few.generator.usecase.GroupSchedulingUseCase
import com.few.generator.usecase.RawContentsBrowseContentUseCase
import com.few.generator.usecase.SendNewsletterUseCase
import com.few.generator.usecase.input.BrowseContentsUseCaseIn
import com.few.web.ApiResponse
import com.few.web.ApiResponseGenerator
import jakarta.validation.constraints.Min
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/api/v1")
class ContentsGeneratorController(
    private val genSchedulingUseCase: GenSchedulingUseCase,
    private val globalGenSchedulingUseCase: GlobalGenSchedulingUseCase,
    private val newsletterSchedulingUseCase: SendNewsletterUseCase,
    private val rawContentsBrowseContentUseCase: RawContentsBrowseContentUseCase,
    private val browseContentsUseCase: BrowseContentsUseCase,
    private val groupGenBrowseUseCase: GroupGenBrowseUseCase,
    private val groupSchedulingUseCase: GroupSchedulingUseCase,
) {
    @PostMapping(
        value = ["/contents/schedule"],
    )
    fun createAll(
        @Validated @RequestBody(required = false) request: ContentsSchedulingRequest,
    ): ApiResponse<ApiResponse.Success> {
        when (request.type.uppercase()) {
            ContentsType.GLOBAL_NEWS.title.uppercase() -> globalGenSchedulingUseCase.execute()
            ContentsType.LOCAL_NEWS.title.uppercase() -> genSchedulingUseCase.execute()
            else -> throw BadRequestException("Invalid Contents Type: ${request.type}")
        }

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/send"],
    )
    fun sendAll(): ApiResponse<ApiResponse.Success> {
        newsletterSchedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @PostMapping(
        value = ["/contents/groups/schedule"],
    )
    fun createAllGroupGen(): ApiResponse<ApiResponse.Success> {
        groupSchedulingUseCase.execute()

        return ApiResponseGenerator.success(
            HttpStatus.OK,
        )
    }

    @GetMapping(
        value = [
            "/contents", // TODO: remove deprecated URL "contents"
            "/contents/local-news",
            "/contents/global-news",
        ],
    )
    fun readLocalNewsContents(
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
                        thumbnailImageUrl =
                            useCaseOut.rawContents.thumbnailImageUrl,
                        rawTexts = useCaseOut.rawContents.rawTexts,
                        imageUrls = useCaseOut.rawContents.imageUrls,
                        mediaType =
                            CodeValueResponse(
                                code =
                                    useCaseOut
                                        .rawContents
                                        .mediaType
                                        .code,
                                value =
                                    useCaseOut
                                        .rawContents
                                        .mediaType
                                        .title,
                            ),
                        createdAt = useCaseOut.rawContents.createdAt,
                    ),
                provisioningContents =
                    BrowseProvisioningContentsResponse(
                        id = useCaseOut.provisioningContents.id,
                        rawContentsId =
                            useCaseOut.provisioningContents.rawContentsId,
                        completionIds =
                            useCaseOut.provisioningContents.completionIds,
                        bodyTextsJson =
                            useCaseOut.provisioningContents.bodyTextsJson,
                        coreTextsJson =
                            useCaseOut.provisioningContents.coreTextsJson,
                        createdAt = useCaseOut.provisioningContents.createdAt,
                    ),
                gen =
                    BrowseGenResponse(
                        id = useCaseOut.gen.id,
                        provisioningContentsId =
                            useCaseOut.gen.provisioningContentsId,
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
    fun getCategories(): ApiResponse<ApiResponse.SuccessBody<List<CodeValueResponse>>> =
        ApiResponseGenerator.success(
            Category.entries.map { CodeValueResponse(code = it.code, value = it.title) },
            HttpStatus.OK,
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
        val response = groupGenBrowseUseCase.execute(date)
        return ApiResponseGenerator.success(response, HttpStatus.OK)
    }

    @GetMapping(value = ["/contents/types"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getContentsTypes(): ApiResponse<ApiResponse.SuccessBody<List<String>>> =
        ApiResponseGenerator.success(
            ContentsType.entries.map { it.title },
            HttpStatus.OK,
        )
}