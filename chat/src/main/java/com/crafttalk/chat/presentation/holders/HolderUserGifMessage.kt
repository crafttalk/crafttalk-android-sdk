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
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.GifMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserGifMessage(
    view: View,
    private val download: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (gifName: String, gifUrl: String) -> Unit
) : BaseViewHolder<GifMessageItem>(view), View.OnClickListener {
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.user_gif_warning)

    private val gif: ImageView? = view.findViewById(R.id.user_gif)
    private val downloadGif: TextView? = view.findViewById(R.id.download_file)
    private val author: TextView? = view.findViewById(R.id.author)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var gifUrl: String? = null
    private var gifName: String? = null
    private var failLoading: Boolean = false

    init {
        gif?.setOnClickListener(this)
        downloadGif?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.user_gif -> {
                if (!failLoading) {
                    val name = gifName ?: return
                    val url = gifUrl ?: return
                    clickHandler(name, url)
                }
            }
            R.id.download_file -> {
                val name = gifName ?: return
                val url = gifUrl ?: return
                download(name, url, TypeFile.GIF)
            }
        }
    }

    override fun bindTo(item: GifMessageItem) {
        gifUrl = item.gif.url
        gifName = item.gif.name
        failLoading = item.gif.failLoading

        date?.setDate(item)
        // set content
        author?.setAuthor(item)
        authorPreview?.setAuthorIcon(showAuthorIcon = false)
        time?.setTime(item)
        status?.setStatusMessage(item)
        gif?.apply {
            settingMediaFile()
            loadMediaFile(item.idKey, item.gif, updateData, true, warningContainer, true)
        }
        downloadGif?.settingDownloadBtn(true, failLoading)
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgUserMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMediaFileMessage))
        }
    }

}