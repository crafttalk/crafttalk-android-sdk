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
    private val download: (fileName: String?, fileUrl: String?, fileType: TypeFile) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickHandler: (imageUrl: String) -> Unit
) : BaseViewHolder<ImageMessageItem>(view), View.OnClickListener {
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.server_image_warning)

    private val img: ImageView? = view.findViewById(R.id.server_image)
    private val downloadImage: TextView? = view.findViewById(R.id.download_file)
    private val author: TextView? = view.findViewById(R.id.author)
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
                imageUrl?.let {
                    if (!failLoading)
                        clickHandler(it)
                }
            }
            R.id.download_file -> {
                download(imageName, imageUrl, TypeFile.IMAGE)
            }
        }
    }

    override fun bindTo(item: ImageMessageItem) {
        imageUrl = item.image.url
        imageName = item.image.name
        failLoading = item.image.failLoading

        date?.setDate(item)
        // set content
        author?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        img?.apply {
            settingMediaFile()
            loadMediaFile(item.idKey, item.image, updateData, false, warningContainer)
        }
        downloadImage?.settingDownloadBtn(false, failLoading)
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMediaFileMessage))
        }
    }

}