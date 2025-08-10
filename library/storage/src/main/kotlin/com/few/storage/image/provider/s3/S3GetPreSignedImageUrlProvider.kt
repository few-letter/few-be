package com.few.storage.image.provider.s3

import com.few.storage.image.GetPreSignedImageUrlProvider
import com.few.storage.image.client.ImageStoreClient
import com.few.storage.image.client.util.ImageArgsGenerator

class S3GetPreSignedImageUrlProvider(
    val bucket: String,
    private val imageStoreClient: ImageStoreClient,
) : GetPreSignedImageUrlProvider {
    override fun execute(image: String): String? {
        ImageArgsGenerator.preSignedUrl(bucket, image).let { args ->
            return imageStoreClient.getPreSignedObjectUrl(args)
        }
    }
}