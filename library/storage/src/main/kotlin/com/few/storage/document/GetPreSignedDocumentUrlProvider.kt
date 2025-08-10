package com.few.storage.document

import com.few.storage.GetPreSignedObjectUrlProvider

interface GetPreSignedDocumentUrlProvider : GetPreSignedObjectUrlProvider {
    override fun execute(image: String): String?
}