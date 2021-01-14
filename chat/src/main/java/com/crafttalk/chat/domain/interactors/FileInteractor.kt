package com.crafttalk.chat.domain.interactors

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.repository.IFileRepository
import javax.inject.Inject

class FileInteractor
@Inject constructor(
    private val fileRepository: IFileRepository
) {

    fun uploadFile(file: File, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            fileRepository.uploadFile(file, TypeUpload.MULTIPART)
            success()
        } catch (ex: Throwable) {
            fail(ex)
        }
    }

    fun uploadImage(bitmap: Bitmap, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            fileRepository.uploadFile(bitmap, TypeUpload.MULTIPART)
            success()
        } catch (ex: Throwable) {
            fail(ex)
        }
    }

    fun uploadFiles(listFile: List<File>, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        listFile.forEach {
            uploadFile(it, success, fail)
        }
    }

}