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
import com.crafttalk.chat.presentation.model.ImageMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorImageMessage(
    view: View,
    private val download: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit,
    private val updateData: (id: String, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageName: String, imageUrl: String) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val contentContainer: View? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.server_image_warning)

    private val img: ImageView? = view.findViewById(R.id.server_image)
    private val downloadImage: TextView? = view.findViewById(R.id.download_file)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var imageUrl: String? = null
    private var imageName: String? = null
    private var failLoading: Boolean = false

    init {
        img?.setOnClickListener(this)
        downloadImage?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.server_image -> {
                if (!failLoading) {
                    val name = imageName ?: return
                    val url = imageUrl ?: return
                    clickHandler(name, url)
                }
            }
            R.id.download_file -> {
                val name = imageName ?: return
                val url = imageUrl ?: return
                download(name, url, TypeFile.IMAGE)
            }
        }
    }

    override fun bindTo(item: ImageMessageItem) {
        imageUrl = item.image.url
        imageName = item.image.name.toString()
        failLoading = item.image.failLoading

        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        img?.apply {
            settingMediaFile()
            loadMediaFile(item.id, item.image, updateData, false, false, warningContainer)
        }
        downloadImage?.settingDownloadBtn(false, failLoading)
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMediaFileMessage))
        }
    }

}