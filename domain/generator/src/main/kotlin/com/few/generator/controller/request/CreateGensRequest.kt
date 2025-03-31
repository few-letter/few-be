package com.few.generator.controller.request

data class CreateGensRequest(
    val provContentsId: Long,
    val types: Set<Int>,
)