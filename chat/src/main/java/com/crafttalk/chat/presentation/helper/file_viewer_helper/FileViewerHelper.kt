package com.crafttalk.chat.presentation.helper.file_viewer_helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.helper.permission.checkPermission
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.utils.ChatParams
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileViewerHelper {

    fun pickFiles(
        pickFile: ActivityResultLauncher<Pair<TypeFile, TypeMultiple>>?,
        pickSettings: Pair<TypeFile, TypeMultiple>,
        noPermission: (permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit) -> Unit,
        fragment: Fragment
    ) {
        fun pickFile() {
            pickFile?.launch(pickSettings)
        }
        val permissions = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        checkPermission(
            permissions,
            fragment.requireContext(),
            { noPermission(permissions) { pickFile() } }
        ) { pickFile() }
    }

    fun pickImageFromCamera(
        takePicture: ActivityResultLauncher<Uri>?,
        noPermission: (permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit) -> Unit,
        fragment: Fragment
    ) {
        fun pickImage() {
            ChatParams.fileProviderAuthorities ?: throw Resources.NotFoundException("You haven't set the value of the fileProviderAuthorities attribute!")
            val fileUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                ChatParams.fileProviderAuthorities!!,
                createImageFile(fragment.requireContext(), IMAGE_JPG_FORMAT)
            )
            takePicture?.launch(fileUri)
        }
        val permissions = arrayOf(Manifest.permission.CAMERA)
        checkPermission(
            permissions,
            fragment.requireContext(),
            { noPermission(permissions) { pickImage() } }
        ) { pickImage() }
    }

    fun getUriForFile(context: Context, file: File): Uri = FileProvider
        .getUriForFile(
            context,
            ChatParams.fileProviderAuthorities!!,
            file
        )

    fun getMimeType(context: Context, uri: Uri): String? = context.contentResolver.getType(uri)

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(context: Context, format: String): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File.createTempFile("MEDIA_${timeStamp}_", format, context.filesDir)
    }

    companion object {
        const val PHOTOS_LIMIT_EXCEEDED = 1
        const val DOCUMENTS_LIMIT_EXCEEDED = 1
        const val PHOTOS_LIMIT = 99
        const val DOCUMENTS_LIMIT = 99
        private const val IMAGE_JPG_FORMAT = ".jpg"

        fun showFileLimitExceededMessage(fragment: Fragment, limit: Int) {
            Toast.makeText(
                fragment.requireContext(),
                fragment.resources.getString(
                    R.string.com_crafttalk_chat_bottom_sheet_file_viewer_warning,
                    limit
                ),
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}

/** Требуется для загрузки документов
 * **/
interface OnActivityResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}