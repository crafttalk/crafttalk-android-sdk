package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.ImageMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorImageMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageUrl: String) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val container: ViewGroup? = view.findViewById(R.id.container)
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)

    private val img: ImageView? = view.findViewById(R.id.server_image)
    private val author: TextView? = view.findViewById(R.id.author)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

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
        imageUrl = item.image.url

        date?.setDate(item)
        // set content
        author?.setAuthor(item, true)
        time?.setTime(item)
        status?.setStatusMessage(item)
        img?.apply {
            settingMediaFile(item.image, imageUrl, container)
            loadMediaFile(item.idKey, item.image, updateData)
        }
        // set bg
        contentContainer?.apply {
            setBackgroundResource(R.drawable.background_item_simple_server_message)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }

}