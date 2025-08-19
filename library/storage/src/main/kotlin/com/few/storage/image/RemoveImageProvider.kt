package com.few.storage.image

import com.few.storage.RemoveObjectProvider

fun interface RemoveImageProvider : RemoveObjectProvider {
    override fun execute(image: String): Boolean
}