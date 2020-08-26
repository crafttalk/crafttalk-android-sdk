package com.crafttalk.chat.domain.usecase.file

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.repository.IFileRepository
import javax.inject.Inject

class UploadFiles
@Inject constructor(
    private val fileRepository: IFileRepository
) {

    suspend operator fun invoke(file: File, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            fileRepository.uploadFile(file, TypeUpload.MULTIPART)
            success()
        } catch (ex: Throwable) {
            fail(ex)
        }
    }

    suspend operator fun invoke(bitmap: Bitmap, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            fileRepository.uploadFile(bitmap, TypeUpload.MULTIPART)
            success()
        } catch (ex: Throwable) {
            fail(ex)
        }
    }

    suspend operator fun invoke(listFile: List<File>, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        listFile.forEach {
            invoke(it, success, fail)
        }
    }

}