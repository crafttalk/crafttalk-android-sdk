package com.crafttalk.chat.presentation.helper.file_viewer_helper

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.helper.file_viewer_helper.camera.PickCameraContract
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.PickFileContract
import com.crafttalk.chat.presentation.helper.permission.PermissionHelper
import com.crafttalk.chat.presentation.model.TypeMultiple

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
        resultHandler: (Bitmap) -> Unit,
        noPermission: (permissions: Array<String>) -> Unit,
        fragment: Fragment
    ) {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        permissionHelper.checkPermission(permissions, fragment.requireContext(), { noPermission(permissions) }) {
            val getContent = fragment.registerForActivityResult(
                PickCameraContract()
            ) {
                it?.let(resultHandler)
            }
            getContent.launch(0)
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

    companion object {
        const val PHOTO_LIMIT_EXCEEDED = 1
        private const val PHOTOS_LIMIT = 5
    }
}
