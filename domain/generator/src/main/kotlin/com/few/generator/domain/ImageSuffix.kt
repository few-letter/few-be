package com.few.generator.domain

enum class ImageSuffix {
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png"),
    GIF(".gif"),
    WEBP(".webp"),
    SVG(".svg"),
    ;

    val extension: String
    constructor(extension: String) {
        this.extension = extension
    }

    companion object {
        fun from(extension: String): ImageSuffix? = ImageSuffix.entries.find { it.name.equals(extension, ignoreCase = true) }
    }
}