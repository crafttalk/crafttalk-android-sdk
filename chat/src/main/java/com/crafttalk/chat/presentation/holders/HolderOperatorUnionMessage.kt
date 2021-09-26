package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.UnionMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorUnionMessage(
    view: View,
    private val selectAction: (messageId: String, actionId: String) -> Unit,
    private val download: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit,
    private val updateData: (id: String, height: Int, width: Int) -> Unit,
    private val clickGifHandler: (gifName: String, gifUrl: String) -> Unit,
    private val clickImageHandler: (imageName: String, imageUrl: String) -> Unit,
    private val clickDocumentHandler: (id: String, documentName: String, documentUrl: String) -> Unit
) : BaseViewHolder<UnionMessageItem>(view), View.OnClickListener {
    private val contentContainer: View? = view.findViewById(R.id.content_container)
    private val warningContainer: ViewGroup? = view.findViewById(R.id.server_media_warning)
    private val fileInfo: ViewGroup? = view.findViewById(R.id.file_info)

    private val message: TextView? = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView? = view.findViewById(R.id.actions_list)
    private val fileIcon: ImageView? = view.findViewById(R.id.file_icon)
    private val progressDownload: ProgressBar? = view.findViewById(R.id.progress_download)
    private val fileName: TextView? = view.findViewById(R.id.file_name)
    private val fileSize: TextView? = view.findViewById(R.id.file_size)
    private val media: ImageView? = view.findViewById(R.id.server_media)
    private val downloadMediaFile: TextView? = view.findViewById(R.id.download_file)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var id: String? = null
    private var attachmentFileName: String? = null
    private var attachmentFileUrl: String? = null
    private var mediaFileName: String? = null
    private var fileType: TypeFile? = null
    private var failLoading: Boolean = false

    init {
        fileIcon?.setOnClickListener(this)
        media?.setOnClickListener(this)
        downloadMediaFile?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.file_icon -> {
                val correctId = id ?: return
                val correctDocumentName = attachmentFileName ?: return
                val correctDocumentUrl = attachmentFileUrl ?: return
                if (!failLoading)
                    clickDocumentHandler(correctId, correctDocumentName, correctDocumentUrl)
            }
            R.id.server_media -> {
                if (!failLoading) {
                    val name = mediaFileName ?: return
                    val url = attachmentFileUrl ?: return
                    when (fileType) {
                        TypeFile.IMAGE -> clickImageHandler(name, url)
                        TypeFile.GIF -> clickGifHandler(name, url)
                    }
                }
            }
            R.id.download_file -> {
                val name = mediaFileName ?: return
                val url = attachmentFileUrl ?: return
                download(name, url, fileType ?: TypeFile.IMAGE)
            }
        }
    }

    override fun bindTo(item: UnionMessageItem) {
        id = item.id
        attachmentFileName = item.file.name
        attachmentFileUrl = item.file.url
        mediaFileName = item.file.name
        fileType = item.file.type
        failLoading = item.file.failLoading

        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.apply {
            // set behavior
            setTextIsSelectable(true)
            movementMethod = LinkMovementMethod.getInstance()
            // set width item
            ChatAttr.getInstance().widthItemOperatorTextMessage?.let {
                maxWidth = it
            }
            // set content
            text = item.message
            listActions?.apply {
                if (item.actions == null) {
                    visibility = View.GONE
                } else {
                    adapter = AdapterAction(item.id, item.hasSelectedAction, selectAction).apply {
                        this.data = item.actions
                    }
                    visibility = View.VISIBLE
                }
            }
            // set color
            setTextColor(ChatAttr.getInstance().colorTextOperatorMessage)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextOperatorMessage)
            // set font
            ChatAttr.getInstance().resFontFamilyOperatorMessage?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        downloadMediaFile?.apply {
            if (fileType in listOf(TypeFile.IMAGE, TypeFile.GIF)) {
                settingDownloadBtn(false, failLoading)
            } else {
                visibility = View.GONE
            }
        }
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }

        when (fileType) {
            TypeFile.FILE -> {
                media?.visibility = View.GONE
                warningContainer?.visibility = View.GONE
                fileInfo?.visibility = View.VISIBLE
                progressDownload?.setProgressDownloadFile(item.typeDownloadProgress)
                fileIcon?.setFileIcon(item.typeDownloadProgress)
                fileName?.setFileName(item.file, false)
                fileSize?.setFileSize(item.file, false)
            }
            TypeFile.IMAGE -> {
                fileInfo?.visibility = View.GONE
                media?.apply {
                    visibility = View.VISIBLE
                    settingMediaFile(true)
                    loadMediaFile(item.id, item.file, updateData, false, true, warningContainer)
                }
            }
            TypeFile.GIF -> {
                fileInfo?.visibility = View.GONE
                media?.apply {
                    visibility = View.VISIBLE
                    settingMediaFile(true)
                    loadMediaFile(item.id, item.file, updateData, false, true, warningContainer, true)
                }
            }
        }
    }

}