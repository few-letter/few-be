package com.few.storage.document

import com.few.storage.PutObjectProvider
import com.few.storage.document.client.dto.DocumentWriteResponse
import java.io.File

interface PutDocumentProvider : PutObjectProvider<DocumentWriteResponse> {
    override fun execute(
        name: String,
        file: File,
    ): DocumentWriteResponse?
}