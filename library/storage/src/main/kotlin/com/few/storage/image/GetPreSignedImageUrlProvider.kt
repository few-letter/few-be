package com.few.storage.image

import com.few.storage.GetPreSignedObjectUrlProvider

fun interface GetPreSignedImageUrlProvider : GetPreSignedObjectUrlProvider {
    override fun execute(image: String): String?
}