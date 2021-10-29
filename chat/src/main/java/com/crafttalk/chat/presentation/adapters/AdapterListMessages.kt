package com.crafttalk.chat.presentation.adapters

import android.app.Activity
import android.view.ViewGroup
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.base.BaseAdapterWithPagination
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.*
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr

class AdapterListMessages(
    private val downloadOrOpenDocument: (id: String, documentName: String, documentUrl: String) -> Unit,
    private val openImage: (activity: Activity, imageName: String, imageUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) -> Unit,
    private val openGif: (activity: Activity, gifName: String, gifUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) -> Unit,
    private val downloadFile: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit,
    private val selectAction: (messageId: String, actionId: String) -> Unit,
    private val selectReplyMessage: (messageId: String) -> Unit,
    private val updateData: (id: String, height: Int, width: Int) -> Unit
) : BaseAdapterWithPagination<MessageModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out MessageModel> {
        return when (viewType) {
            R.layout.com_crafttalk_chat_item_user_text_message -> HolderUserTextMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserTextMessage ?: viewType), selectReplyMessage, updateData)
            R.layout.com_crafttalk_chat_item_server_text_message -> HolderOperatorTextMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorTextMessage ?: viewType), selectAction)
            R.layout.com_crafttalk_chat_item_user_image_message -> HolderUserImageMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserImageMessage ?: viewType), downloadFile, updateData)
            { imageName, imageUrl -> openImage(parent.context as Activity, imageName, imageUrl, downloadFile) }
            R.layout.com_crafttalk_chat_item_server_image_message -> HolderOperatorImageMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorImageMessage ?: viewType), downloadFile, updateData)
            { imageName, imageUrl -> openImage(parent.context as Activity, imageName, imageUrl, downloadFile) }
            R.layout.com_crafttalk_chat_item_user_file_message -> HolderUserFileMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserFileMessage ?: viewType))
            { id, documentName, documentUrl -> downloadOrOpenDocument(id, documentName, documentUrl) }
            R.layout.com_crafttalk_chat_item_server_file_message -> HolderOperatorFileMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorFileMessage ?: viewType))
            { id, documentName, documentUrl -> downloadOrOpenDocument(id, documentName, documentUrl) }
            R.layout.com_crafttalk_chat_item_user_gif_message -> HolderUserGifMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserGifMessage ?: viewType), downloadFile, updateData)
            { gifName, gifUrl -> openGif(parent.context as Activity, gifName, gifUrl, downloadFile) }
            R.layout.com_crafttalk_chat_item_server_gif_message -> HolderOperatorGifMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorGifMessage ?: viewType), downloadFile, updateData)
            { gifName, gifUrl -> openGif(parent.context as Activity, gifName, gifUrl, downloadFile) }
            R.layout.com_crafttalk_chat_item_user_union_message -> HolderUserUnionMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserUnionMessage ?: viewType), downloadFile, updateData,
                { gifName, gifUrl -> openGif(parent.context as Activity, gifName, gifUrl, downloadFile) },
                { imageName, imageUrl -> openImage(parent.context as Activity, imageName, imageUrl, downloadFile) },
                { id, documentName, documentUrl -> downloadOrOpenDocument(id, documentName, documentUrl) }
            )
            R.layout.com_crafttalk_chat_item_server_union_message -> HolderOperatorUnionMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorUnionMessage ?: viewType), selectAction, downloadFile, updateData,
                { gifName, gifUrl -> openGif(parent.context as Activity, gifName, gifUrl, downloadFile) },
                { imageName, imageUrl -> openImage(parent.context as Activity, imageName, imageUrl, downloadFile) },
                { id, documentName, documentUrl -> downloadOrOpenDocument(id, documentName, documentUrl) }
            )
            R.layout.com_crafttalk_chat_item_transfer_message -> HolderTransferMessage(parent.inflate(ChatAttr.getInstance().layoutItemTransferMessage ?: viewType))
            else -> HolderDefaultMessage(parent.inflate(R.layout.com_crafttalk_chat_item_default_message))
        }
    }

    fun getMessageTimestampByPosition(position: Int): Long? {
        return getItem(position)?.timestamp
    }

}