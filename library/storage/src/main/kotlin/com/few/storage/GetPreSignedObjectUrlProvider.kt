package com.few.storage

interface GetPreSignedObjectUrlProvider {
    fun execute(image: String): String?
}