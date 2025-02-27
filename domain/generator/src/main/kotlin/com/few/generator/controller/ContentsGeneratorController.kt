package com.few.generator.controller

import com.few.generator.controller.request.WebContentsGeneratorRequest
import com.few.generator.controller.response.ContentsGeneratorResponse
import com.few.generator.usecase.ContentsGeneratorUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import web.ApiResponse
import web.ApiResponseGenerator

@RestController
@RequestMapping("/api/v1")
class ContentsGeneratorController(
    private val contentsGeneratorUseCase: ContentsGeneratorUseCase,
) {
    @PostMapping(value = ["/generators/contents"], consumes = ["application/json"])
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
}