package com.crafttalk.chat.domain.repository

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.entity.file.File as DomainFile
import java.io.File as IOFile

interface IFileRepository {
    fun uploadFile(visitor: Visitor, file: DomainFile, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit)
    fun uploadMediaFile(visitor: Visitor, bitmap: Bitmap, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit)
    suspend fun downloadDocument(documentUrl: String, documentFile: IOFile, alternativeFile: IOFile, downloadedSuccess: suspend () -> Unit, downloadedFail: () -> Unit)
}