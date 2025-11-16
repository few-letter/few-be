package com.few.generator.domain.vo

data class Categories(
    val values: List<Int>,
) {
    companion object {
        fun from(categoriesString: String): Categories {
            val categoryList = categoriesString.split(",").mapNotNull { it.trim().toIntOrNull() }
            return Categories(categoryList)
        }
    }
}