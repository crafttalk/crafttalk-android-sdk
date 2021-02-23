package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadMediaFile
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.helper.extensions.settingMediaFile
import com.crafttalk.chat.presentation.model.GifMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserGifMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (gifUrl: String) -> Unit
) : BaseViewHolder<GifMessageItem>(view), View.OnClickListener {
    private val container: ViewGroup = view.findViewById(R.id.container)
    private val gifContainer: ViewGroup = view.findViewById(R.id.user_gif_container)
    private val gif: ImageView = view.findViewById(R.id.user_gif)
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
        gif.settingMediaFile(item.gif, gifUrl, container)
        gifUrl = item.gif.url
        gif.loadMediaFile(item.idKey, item.gif, updateData, true)
        time.setTimeMessageWithCheck(item)
        // set bg
        ViewCompat.setBackgroundTintList(gifContainer, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))
    }

}