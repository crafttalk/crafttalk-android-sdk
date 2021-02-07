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

    fun uploadFile(file: File, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        fileRepository.uploadFile(visitor, file, TypeUpload.MULTIPART, handleUploadFile)
    }

    fun uploadImage(bitmap: Bitmap, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        fileRepository.uploadMediaFile(visitor, bitmap, TypeUpload.MULTIPART, handleUploadFile)
    }

    fun uploadFiles(listFile: List<File>, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        listFile.forEach {
            uploadFile(it, handleUploadFile)
        }
    }

}