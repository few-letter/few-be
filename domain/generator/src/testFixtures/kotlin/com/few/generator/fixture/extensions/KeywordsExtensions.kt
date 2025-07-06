package com.few.generator.fixture.extensions

import com.few.generator.core.gpt.prompt.schema.Keywords

/**
 * Keywords 관련 확장 함수들
 */
fun Keywords.asString(): String = keywords.joinToString(", ")