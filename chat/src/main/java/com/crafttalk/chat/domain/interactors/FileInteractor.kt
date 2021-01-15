package com.crafttalk.chat.domain.interactors

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.repository.IFileRepository
import javax.inject.Inject

class FileInteractor
@Inject constructor(
    private val fileRepository: IFileRepository,
    private val visitorInteractor: VisitorInteractor
) {

    fun uploadFile(file: File, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        try {
            fileRepository.uploadFile(visitor, file, TypeUpload.MULTIPART)
            success()
        } catch (ex: Throwable) {
            fail(ex)
        }
    }

    fun uploadImage(bitmap: Bitmap, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        try {
            fileRepository.uploadFile(visitor, bitmap, TypeUpload.MULTIPART)
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