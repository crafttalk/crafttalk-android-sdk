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
    private val clickHandler: (gifUrl: String) -> Unit
) : BaseViewHolder<GifMessageItem>(view), View.OnClickListener {
    private val gif: ImageView = view.findViewById(R.id.server_image)
    private val time: TextView = view.findViewById(R.id.time)
    private var gifUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        gifUrl?.let{
            clickHandler(it)
        }
    }

    override fun bindTo(item: GifMessageItem) {
        gifUrl = item.gif.url
        gif.loadGif(item.idKey, item.gif, updateData)
        time.setTimeMessageWithCheck(item)
    }

}