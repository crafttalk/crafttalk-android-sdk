package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadImage
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageDefault
import com.crafttalk.chat.presentation.model.ImageMessageItem

class HolderOperatorImageMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageUrl: String, width: Int, height: Int) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val img: ImageView = view.findViewById(R.id.server_image)
    private val time: TextView = view.findViewById(R.id.time)
    private var imageUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        imageUrl?.let{
            clickHandler(it, img.width, img.height)
        }
    }

    override fun bindTo(item: ImageMessageItem) {
        imageUrl = item.imageUrl
        img.loadImage(item, updateData)
        time.setTimeMessageDefault(item)
    }

}