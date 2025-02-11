package com.crafttalk.chat.domain.interactors

import android.graphics.Bitmap
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.File as DomainFile
import java.io.File as IOFile
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.repository.IFileRepository
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class FileInteractor
@Inject constructor(
    private val fileRepository: IFileRepository,
    private val messageRepository: IMessageRepository,
    private val visitorInteractor: VisitorInteractor
) {

    fun uploadFile(file: DomainFile, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        fileRepository.uploadFile(visitor, file, TypeUpload.MULTIPART, handleUploadFile)
    }

    fun uploadImage(bitmap: Bitmap, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val visitor = visitorInteractor.getVisitor() ?: return
        fileRepository.uploadMediaFile(visitor, bitmap, TypeUpload.MULTIPART, handleUploadFile)
    }

    fun uploadFiles(listFile: List<DomainFile>, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        listFile.forEach {
            uploadFile(it, handleUploadFile)
        }
    }

    suspend fun downloadDocument(
        id: String,
        documentName: String,
        documentUrl: String,
        directory: IOFile,
        openDocument: suspend (file: IOFile) -> Unit,
        downloadedFailed: () -> Unit
    ) {
        val documentFile = IOFile(directory, "${id}_${documentName}")
        val alternativeFile = IOFile(directory, "${id}.${documentName.split(".").last()}")

        when {
            documentFile.exists() -> openDocument(documentFile)
            alternativeFile.exists() -> openDocument(alternativeFile)
            else -> {
                messageRepository.updateTypeDownloadProgressOfMessageWithAttachment(id, TypeDownloadProgress.DOWNLOADING)
                fileRepository.downloadDocument(
                    documentUrl = documentUrl,
                    documentFile = documentFile,
                    alternativeFile = alternativeFile,
                    downloadedSuccess = {
                        messageRepository.updateTypeDownloadProgressOfMessageWithAttachment(id, TypeDownloadProgress.DOWNLOADED)
                        openDocument(documentFile)
                    },
                    downloadedFail = {
                        messageRepository.updateTypeDownloadProgressOfMessageWithAttachment(id, TypeDownloadProgress.NOT_DOWNLOADED)
                        downloadedFailed()
                    }
                )
            }
        }
    }

}