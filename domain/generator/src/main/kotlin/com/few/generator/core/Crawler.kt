package com.few.generator.core

import com.few.generator.core.model.ContentSpec

interface Crawler {
    fun execute(url: String): ContentSpec?
}