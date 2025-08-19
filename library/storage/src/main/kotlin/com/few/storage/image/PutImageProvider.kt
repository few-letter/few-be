package com.few.storage.image

import com.few.storage.PutObjectProvider
import com.few.storage.image.client.dto.ImageWriteResponse
import java.io.File

fun interface PutImageProvider : PutObjectProvider<ImageWriteResponse> {
    override fun execute(
        name: String,
        file: File,
    ): ImageWriteResponse?
}