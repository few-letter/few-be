package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CheckPublishableContentResponse(
    val hasPublishableContent: Boolean,
    val count: Int,
    val contentsType: List<String>?,
)