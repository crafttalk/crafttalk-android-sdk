package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadGif
import com.crafttalk.chat.presentation.helper.extensions.loadImage
import com.crafttalk.chat.presentation.helper.extensions.setFileIcon
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.model.UnionMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserUnionMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickGifHandler: (gifUrl: String) -> Unit,
    private val clickImageHandler: (imageUrl: String) -> Unit,
    private val clickDocumentHandler: (fileUrl: String) -> Unit
) : BaseViewHolder<UnionMessageItem>(view), View.OnClickListener {
    private val message: TextView = view.findViewById(R.id.user_message)
    private val time: TextView = view.findViewById(R.id.time)
    private val fileIcon: ImageView = view.findViewById(R.id.user_file)
    private val media: ImageView = view.findViewById(R.id.user_media)

    private var fileUrl: String? = null
    private var fileType: TypeFile? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        fileUrl?.let{ url ->
            when (fileType) {
                TypeFile.FILE -> {
                    clickDocumentHandler(url)
                }
                TypeFile.IMAGE -> {
                    clickImageHandler(url)
                }
                TypeFile.GIF -> {
                    clickGifHandler(url)
                }
                else -> {}
            }
        }
    }

    override fun bindTo(item: UnionMessageItem) {
        fileUrl = item.file.url
        fileType = item.file.type

        time.setTimeMessageWithCheck(item)

        // set content
        message.text = item.message
        // set color
        message.setTextColor(ChatAttr.getInstance().colorTextUserMessage)
        // set dimension
        message.textSize = ChatAttr.getInstance().sizeTextUserMessage
        // set bg
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))

        when (fileType) {
            TypeFile.FILE -> {
                media.visibility = View.GONE
                fileIcon.visibility = View.VISIBLE
                fileIcon.setFileIcon()
            }
            TypeFile.IMAGE -> {
                fileIcon.visibility = View.GONE
                media.visibility = View.VISIBLE
                media.loadImage(item.idKey, item.file, updateData)
            }
            TypeFile.GIF -> {
                fileIcon.visibility = View.GONE
                media.visibility = View.VISIBLE
                media.loadGif(item.idKey, item.file, updateData)
            }
        }

    }

}