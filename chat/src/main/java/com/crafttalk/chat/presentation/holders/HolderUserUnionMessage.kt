package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.UnionMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserUnionMessage(
    view: View,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickGifHandler: (gifUrl: String) -> Unit,
    private val clickImageHandler: (imageUrl: String) -> Unit,
    private val clickDocumentHandler: (fileUrl: String) -> Unit
) : BaseViewHolder<UnionMessageItem>(view), View.OnClickListener {
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.user_media_warning)

    private val message: TextView? = view.findViewById(R.id.user_message)
    private val author: TextView? = view.findViewById(R.id.author)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val fileIcon: ImageView? = view.findViewById(R.id.user_file)
    private val fileName: TextView? = view.findViewById(R.id.user_file_name)
    private val fileSize: TextView? = view.findViewById(R.id.user_file_size)
    private val media: ImageView? = view.findViewById(R.id.user_media)
    private val date: TextView? = view.findViewById(R.id.date)

    private var fileUrl: String? = null
    private var fileType: TypeFile? = null
    private var failLoading: Boolean = false

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        fileUrl?.let{ url ->
            when (fileType) {
                TypeFile.FILE -> {
                    if (!failLoading)
                        clickDocumentHandler(url)
                }
                TypeFile.IMAGE -> {
                    if (!failLoading)
                        clickImageHandler(url)
                }
                TypeFile.GIF -> {
                    if (!failLoading)
                        clickGifHandler(url)
                }
                else -> {}
            }
        }
    }

    override fun bindTo(item: UnionMessageItem) {
        fileUrl = item.file.url
        fileType = item.file.type
        failLoading = item.file.failLoading

        date?.setDate(item)
        // set content
        author?.setAuthor(item)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.apply {
            // set behavior
            setTextIsSelectable(true)
            movementMethod = LinkMovementMethod.getInstance()
            // set width item
            ChatAttr.getInstance().widthItemUserTextMessage?.let {
                maxWidth = it
            }
            // set content
            text = item.message
            // set color
            setTextColor(ChatAttr.getInstance().colorTextUserMessage)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextUserMessage)
            // set font
            ChatAttr.getInstance().resFontFamilyUserMessage?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgUserMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))
        }

        when (fileType) {
            TypeFile.FILE -> {
                media?.visibility = View.GONE
                warningContainer?.visibility = View.GONE
                fileIcon?.apply {
                    visibility = View.VISIBLE
                    setFileIcon()
                    fileName?.apply {
                        visibility = View.VISIBLE
                        setFileName(item.file)
                    }
                    fileSize?.apply {
                        visibility = View.VISIBLE
                        setFileSize(item.file)
                    }
                }
            }
            TypeFile.IMAGE -> {
                fileIcon?.visibility = View.GONE
                fileName?.visibility = View.GONE
                fileSize?.visibility = View.GONE
                media?.apply {
                    visibility = View.VISIBLE
                    settingMediaFile(true)
                    loadMediaFile(item.idKey, item.file, updateData, warningContainer)
                }
            }
            TypeFile.GIF -> {
                fileIcon?.visibility = View.GONE
                fileName?.visibility = View.GONE
                fileSize?.visibility = View.GONE
                media?.apply {
                    visibility = View.VISIBLE
                    settingMediaFile(true)
                    loadMediaFile(item.idKey, item.file, updateData, warningContainer, true)
                }
            }
        }
    }

}