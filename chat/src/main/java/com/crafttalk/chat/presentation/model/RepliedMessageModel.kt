package com.crafttalk.chat.presentation.model

import android.content.Context
import android.text.SpannableString
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString

data class RepliedMessageModel(
    val id: String,
    val textMessage: SpannableString? = null,
    val file: FileModel? = null
) {

    companion object {

        fun map(localMessage: MessageEntity, context: Context): RepliedMessageModel? {
            val repliedMessageId = localMessage.repliedMessageId ?: return null
            val repliedMessageText = localMessage.repliedMessageText?.convertToSpannableString(!localMessage.isReply, localMessage.repliedTextSpanStructureList, context)

            val isNotAttachmentFile = localMessage.repliedMessageAttachmentUrl == null ||
                    localMessage.repliedMessageAttachmentType == null ||
                    localMessage.repliedMessageAttachmentName == null

            if (repliedMessageText == null && isNotAttachmentFile) {
                return null
            } else {
                val file = if (isNotAttachmentFile) {
                    null
                } else {
                    FileModel(
                        url = localMessage.repliedMessageAttachmentUrl!!,
                        name = SpannableString(localMessage.repliedMessageAttachmentName ?: ""),
                        size = localMessage.repliedMessageAttachmentSize,
                        height = localMessage.repliedMessageAttachmentHeight,
                        width = localMessage.repliedMessageAttachmentWidth,
                        failLoading = (localMessage.repliedMessageAttachmentType in listOf( TypeFile.IMAGE, TypeFile.GIF)) && (localMessage.height == null || localMessage.height == 0 || localMessage.width == null || localMessage.width == 0),
                        type = localMessage.repliedMessageAttachmentType,
                        typeDownloadProgress = localMessage.repliedMessageAttachmentDownloadProgressType ?: TypeDownloadProgress.NOT_DOWNLOADED
                    )
                }

                return RepliedMessageModel(
                    id = repliedMessageId,
                    textMessage = repliedMessageText,
                    file = file
                )
            }
        }

    }

}