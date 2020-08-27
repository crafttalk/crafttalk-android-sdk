package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadGif
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.model.GifMessageItem

class HolderUserGifMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (gifUrl: String, width: Int, height: Int) -> Unit
) : BaseViewHolder<GifMessageItem>(view), View.OnClickListener {
    private val gif: ImageView = view.findViewById(R.id.server_image)
    private val time: TextView = view.findViewById(R.id.time)
    private lateinit var gifUrl: String

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        clickHandler(gifUrl, gif.width, gif.height)
    }

    override fun bindTo(item: GifMessageItem) {
        gifUrl = item.gifUrl
        gif.loadGif(item, updateData)
        time.setTimeMessageWithCheck(item)
    }

}