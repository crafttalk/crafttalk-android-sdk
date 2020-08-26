package com.crafttalk.chat.presentation.adapters

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseAdapter
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.*
import com.crafttalk.chat.presentation.model.MessageModel

class AdapterListMessages(
    private val scaleRatio: Float,
    private val openFile: (context: Context, fileUrl: String) -> Unit,
    private val openImage: (activity: Activity, imageUrl: String, width: Int, height: Int) -> Unit,
    private val openGif: (activity: Activity, gifUrl: String, width: Int, height: Int) -> Unit,
    private val selectAction: (actionId: String) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit
) : BaseAdapter<MessageModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out MessageModel> {
        return when (viewType) {
            R.layout.item_user_text_message -> HolderUserTextMessage(parent.inflate(viewType), scaleRatio)
            R.layout.item_server_text_message -> HolderOperatorTextMessage(parent.inflate(viewType), scaleRatio, selectAction)
            R.layout.item_user_image_message -> HolderUserImageMessage(parent.inflate(viewType), scaleRatio, updateData)
            { imageUrl, width, height -> openImage(parent.context as Activity, imageUrl, width, height) }
            R.layout.item_server_image_message -> HolderOperatorImageMessage(parent.inflate(viewType), scaleRatio, updateData)
            { imageUrl, width, height -> openImage(parent.context as Activity, imageUrl, width, height) }
            R.layout.item_user_file_message -> HolderUserFileMessage(parent.inflate(viewType), scaleRatio)
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_server_file_message -> HolderOperatorFileMessage(parent.inflate(viewType), scaleRatio)
            { fileUrl -> openFile(parent.context, fileUrl) }
            R.layout.item_user_gif_message -> HolderUserGifMessage(parent.inflate(viewType), scaleRatio, updateData)
            { gifUrl, width, height -> openGif(parent.context as Activity, gifUrl, width, height) }
            R.layout.item_server_gif_message -> HolderOperatorGifMessage(parent.inflate(viewType), scaleRatio, updateData)
            { gifUrl, width, height -> openGif(parent.context as Activity, gifUrl, width, height) }
            else -> throw Exception("Fail in AdapterListMessages with MessageViewType")
        }
    }

}