package com.few.api.config.jooq

data class ApiSlowQueryEvent(
    val slowQuery: String,
)