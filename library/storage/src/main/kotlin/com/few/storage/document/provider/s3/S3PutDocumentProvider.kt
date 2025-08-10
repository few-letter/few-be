package com.few.storage.document.provider.s3

import com.few.storage.document.PutDocumentProvider
import com.few.storage.document.client.DocumentStoreClient
import com.few.storage.document.client.dto.DocumentWriteResponse
import com.few.storage.document.client.util.DocumentArgsGenerator
import java.io.File

class S3PutDocumentProvider(
    val bucket: String,
    private val documentStoreClient: DocumentStoreClient,
) : PutDocumentProvider {
    override fun execute(
        name: String,
        file: File,
    ): DocumentWriteResponse? {
        DocumentArgsGenerator.putDocument(bucket, name, file).let { args ->
            return documentStoreClient.putObject(args)
        }
    }
}