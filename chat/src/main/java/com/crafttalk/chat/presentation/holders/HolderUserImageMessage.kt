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
import com.crafttalk.chat.presentation.model.ImageMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserImageMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageUrl: String) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val container: ViewGroup = view.findViewById(R.id.container)
    private val imageContainer: ViewGroup = view.findViewById(R.id.user_image_container)
    private val img: ImageView = view.findViewById(R.id.user_image)
    private val time: TextView = view.findViewById(R.id.time)
    private var imageUrl: String? = null
    private val date: TextView = view.findViewById(R.id.date)

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        imageUrl?.let{
            clickHandler(it)
        }
    }

    override fun bindTo(item: ImageMessageItem) {
        date.setDate(item)
        img.settingMediaFile(item.image, imageUrl, container)
        imageUrl = item.image.url
        img.loadMediaFile(item.idKey, item.image, updateData)
        time.setTimeMessageWithCheck(item)
        // set bg
        ViewCompat.setBackgroundTintList(imageContainer, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))
    }

}