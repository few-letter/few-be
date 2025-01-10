package com.few.generator.usecase.dto

data class ExecuteCrawlerUseCaseOut(
    val sid: Int,
    val crawlingIds: List<String>,
)