package com.few.common.domain

enum class ImageSuffix(
    val extension: String,
) {
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png"),
    GIF(".gif"),
    WEBP(".webp"),
    SVG(".svg"),
    ;

    companion object {
        fun from(extension: String): ImageSuffix? = ImageSuffix.entries.find { it.extension.equals(extension, ignoreCase = true) }
    }
}