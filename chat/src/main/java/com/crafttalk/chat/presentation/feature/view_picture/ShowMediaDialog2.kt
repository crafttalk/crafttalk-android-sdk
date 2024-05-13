package com.crafttalk.chat.presentation.feature.view_picture

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.crafttalk.chat.presentation.DownloadFileListener
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.WarningSnackbar
import com.crafttalk.chat.presentation.helper.extensions.createCorrectGlideUrl
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.MediaFileDownloadMode
import com.crafttalk.chat.presentation.helper.downloaders.downloadResource
import kotlinx.android.synthetic.main.activity_show_media_dialog2.*
import kotlinx.android.synthetic.main.activity_show_media_dialog2.image_navigate_back
import kotlinx.android.synthetic.main.activity_show_media_dialog2.image_show

class ShowMediaDialog2 : AppCompatActivity(),View.OnClickListener {
    private fun settingFileDownload(fileDownload: ImageView) {
        if (ChatAttr.getInstance().mediaFileDownloadMode in listOf(
                MediaFileDownloadMode.ONLY_IN_VIEWER,
                MediaFileDownloadMode.All_PLACES
            )
        ) {
            fileDownload.visibility = View.VISIBLE
            fileDownload.setOnClickListener(this)
        } else {
            fileDownload.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_media_dialog2)
        val filepath: String? = intent.getStringExtra("url")
        setContentView(R.layout.activity_show_media_dialog2)
        image_navigate_back.setOnClickListener(this)
        settingFileDownload(image_download)
        Glide.with(this.applicationContext)
            .load(createCorrectGlideUrl(filepath))
            .error(R.drawable.com_crafttalk_chat_background_item_media_message_placeholder)
            .into(image_show)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.image_navigate_back -> {
                finish()
            }
            R.id.image_download -> {
                val fileName = intent.getStringExtra("imageName")!!
                val fileUrl = intent.getStringExtra("url")!!
                val fileType:TypeFile
                val fileTypeData =  intent.getStringExtra("typeFile")!!
                when (fileTypeData){
                    "IMAGE" -> {fileType = TypeFile.IMAGE}
                    "GIF" -> {fileType = TypeFile.GIF}
                    else -> {fileType = TypeFile.FILE}
                }
                downloadFile?.invoke(fileName, fileUrl, fileType)
                var downloadID: Long? = null
                var storagePermission:Int
                if(Build.VERSION.SDK_INT < 32) {
                    storagePermission = ContextCompat.checkSelfPermission(baseContext, WRITE_EXTERNAL_STORAGE)}
                else{
                    storagePermission = ContextCompat.checkSelfPermission(baseContext, READ_MEDIA_IMAGES)
                }

                if (storagePermission == PackageManager.PERMISSION_GRANTED) {
                    // Permission already granted, proceed with download
                    downloadResource(baseContext, fileName, fileUrl, fileType, downloadFileListener,
                        { permissions: Array<String>, actionsAfterObtainingPermission: () ->
                        Unit ->
                            permissionListener.requestedPermissions(
                                permissions,
                                arrayOf(getString(R.string.com_crafttalk_chat_requested_permission_download)),
                                actionsAfterObtainingPermission
                            )
                        }, { id -> downloadID = id })

                } else {
                    //Request permission
                    //requestStoragePermission()
                    val code:Int = 1
                    if (Build.VERSION.SDK_INT < 32) {
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),code)}
                    else{
                        ActivityCompat.requestPermissions(this, arrayOf(READ_MEDIA_IMAGES),code)
                    }
                }
            }
        }
    }

    private var permissionListener: ChatPermissionListener = object : ChatPermissionListener {
        override fun requestedPermissions(
            permissions: Array<String>,
            messages: Array<String>,
            action: () -> Unit
        ) {
            permissions.forEachIndexed { index, permission ->
                WarningSnackbar.make(
                    view = image_show,
                    title = messages[index]
                )?.show()
            }
        }
    }


    private var downloadFileListener: DownloadFileListener = object : DownloadFileListener {
        override fun successDownload() {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(true)
            } else {
                WarningSnackbar.make(
                    view = image_show,
                    title = ChatAttr.getInstance().titleSuccessDownloadFileWarning,
                    iconRes = R.drawable.com_crafttalk_chat_ic_file_download_done,
                    textColor = ChatAttr.getInstance().colorSuccessDownloadFileWarning,
                    backgroundColor = ChatAttr.getInstance().backgroundSuccessDownloadFileWarning
                )?.show()
            }
        }

        override fun failDownload() {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(false)
            } else {
                WarningSnackbar.make(
                    view = image_show,
                    title = ChatAttr.getInstance().titleFailDownloadFileWarning
                )?.show()
            }
        }
    }

    private var downloadFile: ((fileName: String, fileUrl: String, fileType: TypeFile) -> Unit)? =
        null
}