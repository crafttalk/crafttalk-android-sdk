package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.loadMediaFile
import com.crafttalk.chat.presentation.helper.extensions.setAuthor
import com.crafttalk.chat.presentation.helper.extensions.setAuthorIcon
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setStatusMessage
import com.crafttalk.chat.presentation.helper.extensions.setTime
import com.crafttalk.chat.presentation.helper.extensions.settingDownloadBtn
import com.crafttalk.chat.presentation.helper.extensions.settingMediaFile
import com.crafttalk.chat.presentation.model.StickerMessageItem
import com.crafttalk.chat.utils.ChatAttr


class HolderOperatorStickerMessage(
    view: View,
    private val download: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit,
    private val updateData: (id: String, height: Int, width: Int) -> Unit,
    private val clickHandler: (gifName: String, gifUrl: String) -> Unit
) : BaseViewHolder<StickerMessageItem>(view), View.OnClickListener {
    private val contentContainer: View? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.server_sticker_warning)

    private val sticker: ImageView? = view.findViewById(R.id.server_sticker)
    private val downloadGif: TextView? = view.findViewById(R.id.download_file)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var gifUrl: String? = null
    private var gifName: String? = null
    private var failLoading: Boolean = false

    init {
        sticker?.setOnClickListener(this)
        downloadGif?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.server_gif -> {
                if (!failLoading) {
                    val name = gifName ?: return
                    val url = gifUrl ?: return
                    clickHandler(name, url)
                }
            }
            R.id.download_file -> {
                val name = gifName ?: return
                val url = gifUrl ?: return
                download(name, url, TypeFile.STICKER)
            }
        }
    }

    override fun bindTo(item: StickerMessageItem) {
        gifUrl = item.sticker.url
        gifName = item.sticker.name.toString()
        failLoading = item.sticker.failLoading

        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        sticker?.apply {
            settingMediaFile()
            loadMediaFile(item.id, item.sticker, updateData, false, false, warningContainer, true)
        }
        downloadGif?.settingDownloadBtn(false, failLoading)
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMediaFileMessage))
        }
    }
}