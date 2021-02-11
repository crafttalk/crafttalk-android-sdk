package com.crafttalk.chat.presentation.helper.file_viewer_helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.PickFileContract
import com.crafttalk.chat.presentation.helper.permission.PermissionHelper
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.utils.ChatParams
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileViewerHelper constructor(
    private val permissionHelper: PermissionHelper
) {

    fun pickFiles(
        pickSettings: Pair<TypeFile, TypeMultiple>,
        resultHandler: (List<Uri>) -> Unit,
        noPermission: (permissions: Array<String>) -> Unit,
        fragment: Fragment
    ) {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionHelper.checkPermission(permissions, fragment.requireContext(),  { noPermission(permissions) }) {
            pickFilesFromGallery(pickSettings, fragment, resultHandler)
        }
    }

    fun pickImageFromCamera(
        resultHandler: (Uri) -> Unit,
        noPermission: (permissions: Array<String>) -> Unit,
        fragment: Fragment
    ) {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        permissionHelper.checkPermission(permissions, fragment.requireContext(), { noPermission(permissions) }) {
            ChatParams.fileProviderAuthorities ?: throw Resources.NotFoundException("You haven't set the value of the fileProviderAuthorities attribute!")
            val fileUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                ChatParams.fileProviderAuthorities!!,
                createImageFile(fragment.requireContext(), IMAGE_JPG_FORMAT)
            )
            fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                resultHandler(fileUri)
            }.launch(fileUri)
        }
    }

    private fun pickFilesFromGallery(
        pickSettings: Pair<TypeFile, TypeMultiple>,
        fragment: Fragment,
        resultHandler: (List<Uri>) -> Unit
    ) {
        val getContent = fragment.registerForActivityResult(
            PickFileContract()
        ) {
            if (it.size > PHOTOS_LIMIT) {
                resultHandler(it.slice(0 until PHOTOS_LIMIT))
                showPhotoLimitExceededMessage(fragment)
            } else resultHandler(it)
        }
        getContent.launch(pickSettings)
    }

    private fun showPhotoLimitExceededMessage(fragment: Fragment) {
        Toast.makeText(
            fragment.requireContext(),
            PHOTO_LIMIT_EXCEEDED,
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(context: Context, format: String): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("MEDIA_${timeStamp}_", format, storageDir)
    }

    companion object {
        private const val PHOTO_LIMIT_EXCEEDED = 1
        private const val PHOTOS_LIMIT = 5
        private const val IMAGE_JPG_FORMAT = ".jpg"
    }
}
