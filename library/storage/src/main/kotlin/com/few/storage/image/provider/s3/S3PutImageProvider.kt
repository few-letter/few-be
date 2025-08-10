package com.few.storage.image.provider.s3

import com.few.storage.image.PutImageProvider
import com.few.storage.image.client.ImageStoreClient
import com.few.storage.image.client.dto.ImageWriteResponse
import com.few.storage.image.client.util.ImageArgsGenerator
import java.io.File

class S3PutImageProvider(
    val bucket: String,
    private val imageStoreClient: ImageStoreClient,
) : PutImageProvider {
    override fun execute(
        name: String,
        file: File,
    ): ImageWriteResponse? {
        ImageArgsGenerator.putImage(bucket, name, file).let { args ->
            return imageStoreClient.putObject(args)
        }
    }
}