package com.crafttalk.chat.presentation.feature.view_picture

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.crafttalk.chat.R
import com.crafttalk.chat.databinding.ComCrafttalkChatBottomSheetShowGifBinding
import com.crafttalk.chat.databinding.ComCrafttalkChatBottomSheetShowImageBinding
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.file.TypeFile.GIF
import com.crafttalk.chat.domain.entity.file.TypeFile.IMAGE
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.WarningSnackbar
import com.crafttalk.chat.presentation.helper.extensions.createCorrectGlideUrl
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.MediaFileDownloadMode

class ShowImageDialog(
    context: Context,
    style: Int
): Dialog(context, style), View.OnClickListener {
    private var _bindingGif: ComCrafttalkChatBottomSheetShowGifBinding? = null
    private val bindingGif get() = _bindingGif!!

    private val _bindingImage: ComCrafttalkChatBottomSheetShowImageBinding? = null
    private val bindingImage get() = _bindingImage!!
    override fun onClick(view: View) {
        when(view.id) {
            R.id.image_download, R.id.gif_download -> {
                val fileName = name ?: return
                val fileUrl = url ?: return
                val fileType = type ?: return
                downloadFile?.invoke(fileName, fileUrl, fileType)
            }
            R.id.image_navigate_back, R.id.gif_navigate_back -> dismiss()
        }
    }

    private var name: String? = null
    private var url: String? = null
    private var type: TypeFile? = null
    private var downloadFile: ((fileName: String, fileUrl: String, fileType: TypeFile) -> Unit)? = null

    companion object {
        private var dialog: ShowImageDialog? = null

        private fun newInstance(builder: Builder): ShowImageDialog {
            val dialog = ShowImageDialog(builder.context, R.style.ThemeFullscreen)
            dialog.name = builder.mediaFileName
            dialog.url = builder.mediaFileUrl
            dialog.type = builder.type
            dialog.downloadFile = builder.downloadMediaFile
            this.dialog = dialog
            return dialog
        }

        fun isOpen(): Boolean {
            return dialog != null
        }

        fun showWarning(isSuccess: Boolean) {
            dialog?.let {
                if (isSuccess) {
                    WarningSnackbar.make(
                        view = it.bindingImage.imageShow?: it.bindingGif.gifShow,
                        title = ChatAttr.getInstance().titleSuccessDownloadFileWarning,
                        iconRes = R.drawable.com_crafttalk_chat_ic_file_download_done,
                        textColor = ChatAttr.getInstance().colorSuccessDownloadFileWarning,
                        backgroundColor = ChatAttr.getInstance().backgroundSuccessDownloadFileWarning
                    )?.show()
                } else {
                    WarningSnackbar.make(
                        view = it.bindingImage.imageShow ?: it.bindingGif.gifShow,
                        title = ChatAttr.getInstance().titleFailDownloadFileWarning,
                    )?.show()
                }
            }
        }

    }

    class Builder(val context: Context) {
        var mediaFileName: String? = null
        var mediaFileUrl: String? = null
        var type: TypeFile? = null
        var downloadMediaFile: ((fileName: String, fileUrl: String, fileType: TypeFile) -> Unit)? = null

        fun setName(name: String): Builder {
            this.mediaFileName = name
            return this
        }

        fun setUrl(url: String): Builder {
            this.mediaFileUrl = url
            return this
        }

        fun setType(type: TypeFile): Builder {
            this.type = type
            return this
        }

        fun setFunDownload(downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit): Builder {
            this.downloadMediaFile = downloadFun
            return this
        }

        fun show() {
            mediaFileName ?: return
            mediaFileUrl ?: return
            type ?: return
            newInstance(this).show()
        }

    }

    private fun settingFileDownload(file_download: ImageView) {
        if (ChatAttr.getInstance().mediaFileDownloadMode in listOf(MediaFileDownloadMode.ONLY_IN_VIEWER, MediaFileDownloadMode.All_PLACES)) {
            file_download.visibility = View.VISIBLE
            file_download.setOnClickListener(this)
        } else {
            file_download.visibility = View.GONE
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (type) {
            IMAGE -> {
                setContentView(R.layout.com_crafttalk_chat_bottom_sheet_show_image)
                bindingImage.imageNavigateBack.setOnClickListener(this)
                settingFileDownload(bindingImage.imageDownload)
                Glide.with(context)
                    .load(createCorrectGlideUrl(url))
                    .error(R.drawable.com_crafttalk_chat_background_item_media_message_placeholder)
                    .into(bindingImage.imageShow)
            }
            GIF -> {
                setContentView(R.layout.com_crafttalk_chat_bottom_sheet_show_gif)
                bindingGif.gifNavigateBack.setOnClickListener(this)
                settingFileDownload(bindingGif.gifDownload)
                Glide.with(context)
                    .asGif()
                    .load(createCorrectGlideUrl(url))
                    .error(R.drawable.com_crafttalk_chat_background_item_media_message_placeholder)
                    .into(bindingGif.gifShow)
            }
            TypeFile.STICKER -> {
                setContentView(R.layout.com_crafttalk_chat_bottom_sheet_show_image)
                bindingImage.imageNavigateBack.setOnClickListener(this)
                settingFileDownload(bindingImage.imageDownload)
                Glide.with(context)
                    .load(createCorrectGlideUrl(url))
                    .error(R.drawable.com_crafttalk_chat_background_item_media_message_placeholder)
                    .into(bindingImage.imageShow)
            }
            else -> {}
        }

    }

    override fun dismiss() {
        super.dismiss()
        dialog = null
    }
}