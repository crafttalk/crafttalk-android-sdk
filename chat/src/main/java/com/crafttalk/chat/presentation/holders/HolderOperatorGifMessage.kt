package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadMediaFile
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageDefault
import com.crafttalk.chat.presentation.helper.extensions.settingMediaFile
import com.crafttalk.chat.presentation.model.GifMessageItem

class HolderOperatorGifMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (gifUrl: String) -> Unit
) : BaseViewHolder<GifMessageItem>(view), View.OnClickListener {
    private val gif: ImageView = view.findViewById(R.id.server_image)
    private val time: TextView = view.findViewById(R.id.time)
    private var gifUrl: String? = null
    private val date: TextView = view.findViewById(R.id.date)

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        gifUrl?.let{
            clickHandler(it)
        }
    }

    override fun bindTo(item: GifMessageItem) {
        date.setDate(item)
        gif.settingMediaFile(item.gif, gifUrl, time)
        gifUrl = item.gif.url
        gif.loadMediaFile(item.idKey, item.gif, updateData, true)
        time.setTimeMessageDefault(item, true)
    }

}