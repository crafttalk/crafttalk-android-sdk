package com.crafttalk.chat.domain.repository

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.entity.file.File

interface IFileRepository {
    fun uploadFile(file: File, type: TypeUpload)
    fun uploadFile(bitmap: Bitmap, type: TypeUpload)
}