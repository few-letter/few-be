package com.few.generator.domain

enum class Category(
    val code: Int,
    val title: String,
) {
    TECHNOLOGY(1 shl 1, "Technology"),
    SCIENCE(1 shl 2, "Science"),
    HEALTH(1 shl 3, "Health"),
    BUSINESS(1 shl 4, "Business"),
    ENTERTAINMENT(1 shl 5, "Entertainment"),
    SPORTS(1 shl 6, "Sports"),
    POLITICS(1 shl 7, "Politics"),
    ENVIRONMENT(1 shl 8, "Environment"),
    ECONOMY(1 shl 9, "Economy"),
    CULTURE(1 shl 10, "Culture"),
    TRAVEL(1 shl 11, "Travel"),
    APPLE(1 shl 12, "Apple"),
    EV(1 shl 13, "EV"),
    EDUCATION(1 shl 14, "Education"),
    FOOD(1 shl 15, "Food"),
    ETC(1 shl 16, "Etc"),

    ;

    companion object {
        fun from(code: Int): Category =
            Category.values().find { it.code == code }
                ?: throw IllegalArgumentException("Invalid Category code: $code")

        fun from(title: String): Category =
            values().find { it.title.equals(title, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid Category title: $title")
    }
}