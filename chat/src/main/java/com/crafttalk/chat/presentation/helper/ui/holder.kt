package com.crafttalk.chat.presentation.helper.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.RepliedMessageModel
import com.crafttalk.chat.utils.ChatAttr

fun bindRepliedMessage(
    itemId: String,
    itemRepliedMessage: RepliedMessageModel?,
    isUserMessage: Boolean,
    repliedMessageContainer: ViewGroup?,
    repliedBarrier: View?,
    repliedMessage: TextView?,
    repliedFileInfo: ViewGroup?,
    repliedFileIcon: ImageView?,
    repliedProgressDownload: ProgressBar?,
    repliedFileName: TextView?,
    repliedFileSize: TextView?,
    repliedMediaFile: ImageView?,
    repliedMediaFileWarning: ViewGroup?,
    updateData: (id: String, height: Int, width: Int) -> Unit
) {
    when {
        itemRepliedMessage == null -> {
            repliedMessageContainer?.visibility = View.GONE
        }
        !itemRepliedMessage.textMessage.isNullOrBlank() -> {
            repliedMessageContainer?.visibility = View.VISIBLE
            repliedMessage?.visibility = View.VISIBLE
            repliedFileInfo?.visibility = View.GONE
            repliedMediaFile?.visibility = View.GONE
            repliedMediaFileWarning?.visibility = View.GONE
            repliedBarrier?.setBackgroundColor(if (isUserMessage) ChatAttr.getInstance().colorBarrierUserRepliedMessage else ChatAttr.getInstance().colorBarrierOperatorRepliedMessage)
            repliedMessage?.setMessageText(
                textMessage = itemRepliedMessage.textMessage,
                maxWidthTextMessage = ChatAttr.getInstance().widthItemUserTextMessage,
                colorTextMessage = if (isUserMessage) ChatAttr.getInstance().colorTextUserRepliedMessage else ChatAttr.getInstance().colorTextOperatorRepliedMessage,
                sizeTextMessage = if (isUserMessage) ChatAttr.getInstance().sizeTextUserRepliedMessage else ChatAttr.getInstance().sizeTextOperatorRepliedMessage,
                resFontFamilyMessage = if (isUserMessage) ChatAttr.getInstance().resFontFamilyUserMessage else ChatAttr.getInstance().resFontFamilyOperatorMessage
            )
        }
        itemRepliedMessage.file != null -> {
            when (itemRepliedMessage.file.type) {
                TypeFile.FILE -> {
                    repliedMessageContainer?.visibility = View.VISIBLE
                    repliedMessage?.visibility = View.GONE
                    repliedMediaFile?.visibility = View.GONE
                    repliedMediaFileWarning?.visibility = View.GONE
                    repliedFileInfo?.visibility = View.VISIBLE
                    repliedBarrier?.setBackgroundColor(if (isUserMessage) ChatAttr.getInstance().colorBarrierUserRepliedMessage else ChatAttr.getInstance().colorBarrierOperatorRepliedMessage)
                    repliedProgressDownload?.setProgressDownloadFile(itemRepliedMessage.file.typeDownloadProgress)
                    repliedFileIcon?.setFileIcon(itemRepliedMessage.file.typeDownloadProgress)
                    repliedFileName?.setFileName(
                        file = itemRepliedMessage.file,
                        colorTextFileName = if (isUserMessage) ChatAttr.getInstance().colorUserRepliedFileName else ChatAttr.getInstance().colorOperatorRepliedFileName,
                        sizeTextFileName = if (isUserMessage) ChatAttr.getInstance().sizeUserRepliedFileName else ChatAttr.getInstance().sizeOperatorRepliedFileName
                    )
                    repliedFileSize?.setFileSize(
                        file = itemRepliedMessage.file,
                        colorTextFileSize = if (isUserMessage) ChatAttr.getInstance().colorUserRepliedFileSize else ChatAttr.getInstance().colorOperatorRepliedFileSize,
                        sizeTextFileSize = if (isUserMessage) ChatAttr.getInstance().sizeUserRepliedFileSize else ChatAttr.getInstance().sizeOperatorRepliedFileSize
                    )
                }
                TypeFile.IMAGE -> {
                    repliedMessageContainer?.visibility = View.VISIBLE
                    repliedMessage?.visibility = View.GONE
                    repliedFileInfo?.visibility = View.GONE
                    repliedMediaFile?.apply {
                        visibility = View.VISIBLE
                        repliedBarrier?.setBackgroundColor(if (isUserMessage) ChatAttr.getInstance().colorBarrierUserRepliedMessage else ChatAttr.getInstance().colorBarrierOperatorRepliedMessage)
                        settingMediaFile(true)
                        loadMediaFile(
                            id = itemId,
                            mediaFile = itemRepliedMessage.file,
                            updateData = updateData,
                            isUserMessage = true,
                            isUnionMessage = true,
                            warningContainer = repliedMediaFileWarning
                        )
                    }
                }
                TypeFile.GIF -> {
                    repliedMessageContainer?.visibility = View.VISIBLE
                    repliedMessage?.visibility = View.GONE
                    repliedFileInfo?.visibility = View.GONE
                    repliedMediaFile?.apply {
                        visibility = View.VISIBLE
                        repliedBarrier?.setBackgroundColor(if (isUserMessage) ChatAttr.getInstance().colorBarrierUserRepliedMessage else ChatAttr.getInstance().colorBarrierOperatorRepliedMessage)
                        settingMediaFile(true)
                        loadMediaFile(
                            id = itemId,
                            mediaFile = itemRepliedMessage.file,
                            updateData = updateData,
                            isUserMessage = true,
                            isUnionMessage = true,
                            warningContainer = repliedMediaFileWarning,
                            isGif = true
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}