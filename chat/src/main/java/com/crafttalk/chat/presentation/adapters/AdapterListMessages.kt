package com.crafttalk.chat.presentation.adapters

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseAdapterWithPagination
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.*
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr

class AdapterListMessages(
    private val openFile: (context: Context, fileUrl: String) -> Unit,
    private val openImage: (activity: Activity, imageUrl: String) -> Unit,
    private val openGif: (activity: Activity, gifUrl: String) -> Unit,
    private val selectAction: (messageId: String, actionId: String) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit
) : BaseAdapterWithPagination<MessageModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out MessageModel> {
        return when (viewType) {
            R.layout.item_user_text_message -> HolderUserTextMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserTextMessage ?: viewType))
            R.layout.item_server_text_message -> HolderOperatorTextMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorTextMessage ?: viewType), selectAction)
            R.layout.item_user_image_message -> HolderUserImageMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserImageMessage ?: viewType), updateData)
            { imageUrl -> openImage(parent.context as Activity, imageUrl) }
            R.layout.item_server_image_message -> HolderOperatorImageMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorImageMessage ?: viewType), updateData)
            { imageUrl -> openImage(parent.context as Activity, imageUrl) }
            R.layout.item_user_file_message -> HolderUserFileMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserFileMessage ?: viewType))
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_server_file_message -> HolderOperatorFileMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorFileMessage ?: viewType))
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_user_gif_message -> HolderUserGifMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserGifMessage ?: viewType), updateData)
            { gifUrl -> openGif(parent.context as Activity, gifUrl) }
            R.layout.item_server_gif_message -> HolderOperatorGifMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorGifMessage ?: viewType), updateData)
            { gifUrl -> openGif(parent.context as Activity, gifUrl) }
            R.layout.item_user_union_message -> HolderUserUnionMessage(parent.inflate(ChatAttr.getInstance().layoutItemUserUnionMessage ?: viewType), updateData,
                { gifUrl -> openGif(parent.context as Activity, gifUrl) },
                { imageUrl -> openImage(parent.context as Activity, imageUrl) },
                { fileUrl -> openFile(parent.context, fileUrl) }
            )
            R.layout.item_server_union_message -> HolderOperatorUnionMessage(parent.inflate(ChatAttr.getInstance().layoutItemOperatorUnionMessage ?: viewType), selectAction, updateData,
                { gifUrl -> openGif(parent.context as Activity, gifUrl) },
                { imageUrl -> openImage(parent.context as Activity, imageUrl) },
                { fileUrl -> openFile(parent.context, fileUrl) }
            )
            else -> HolderDefaultMessage(parent.inflate(R.layout.item_default_message))
        }
    }

}