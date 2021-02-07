package com.crafttalk.chat.domain.repository

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.entity.file.File

interface IFileRepository {
    fun uploadFile(visitor: Visitor, file: File, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit)
    fun uploadMediaFile(visitor: Visitor, bitmap: Bitmap, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit)
}