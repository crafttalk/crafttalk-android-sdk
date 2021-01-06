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

class AdapterListMessages(
    private val openFile: (context: Context, fileUrl: String) -> Unit,
    private val openImage: (activity: Activity, imageUrl: String) -> Unit,
    private val openGif: (activity: Activity, gifUrl: String) -> Unit,
    private val selectAction: (actionId: String) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit
) : BaseAdapterWithPagination<MessageModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out MessageModel> {
        return when (viewType) {
            R.layout.item_user_text_message -> HolderUserTextMessage(parent.inflate(viewType))
            R.layout.item_server_text_message -> HolderOperatorTextMessage(parent.inflate(viewType), selectAction)
            R.layout.item_user_image_message -> HolderUserImageMessage(parent.inflate(viewType), updateData)
            { imageUrl, width, height -> openImage(parent.context as Activity, imageUrl) }
            R.layout.item_server_image_message -> HolderOperatorImageMessage(parent.inflate(viewType), updateData)
            { imageUrl, width, height -> openImage(parent.context as Activity, imageUrl) }
            R.layout.item_user_file_message -> HolderUserFileMessage(parent.inflate(viewType))
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_server_file_message -> HolderOperatorFileMessage(parent.inflate(viewType))
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_user_gif_message -> HolderUserGifMessage(parent.inflate(viewType), updateData)
            { gifUrl, width, height -> openGif(parent.context as Activity, gifUrl) }
            R.layout.item_server_gif_message -> HolderOperatorGifMessage(parent.inflate(viewType), updateData)
            { gifUrl, width, height -> openGif(parent.context as Activity, gifUrl) }
            else -> HolderDefaultMessage(parent.inflate(R.layout.item_default_message))
        }
    }

}