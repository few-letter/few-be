package com.few.email

data class NewsletterModel(
    val dateString: String,
    val gensByCategory: List<CategoryModel>,
    val userEmail: String,
    val unsubscribeUrl: String,
    val landingPageUrl: String,
    val instagramUrl: String,
)

data class CategoryModel(
    val categoryCode: Int,
    val categoryName: String,
    val gens: List<GenModel>,
    val categoryEmoji: String,
)

data class GenModel(
    val id: Long,
    val headline: String,
    val summary: String,
    val category: Int,
    val url: String,
    val mediaTypeName: String,
)