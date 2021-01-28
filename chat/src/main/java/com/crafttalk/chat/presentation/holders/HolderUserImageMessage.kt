package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadMediaFile
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.helper.extensions.settingMediaFile
import com.crafttalk.chat.presentation.model.ImageMessageItem

class HolderUserImageMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageUrl: String) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val img: ImageView = view.findViewById(R.id.user_image)
    private val time: TextView = view.findViewById(R.id.time)
    private var imageUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        imageUrl?.let{
            clickHandler(it)
        }
    }

    override fun bindTo(item: ImageMessageItem) {
        img.settingMediaFile(item.image, imageUrl, time)
        imageUrl = item.image.url
        img.loadMediaFile(item.idKey, item.image, updateData)
        time.setTimeMessageWithCheck(item)
    }

}