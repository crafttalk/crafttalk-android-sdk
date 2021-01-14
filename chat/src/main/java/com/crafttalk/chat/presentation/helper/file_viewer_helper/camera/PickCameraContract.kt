package com.crafttalk.chat.presentation.helper.file_viewer_helper.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import androidx.activity.result.contract.ActivityResultContract

class PickCameraContract : ActivityResultContract<Int, Bitmap?>() {

    override fun createIntent(context: Context, input: Int?): Intent = Intent(ACTION_IMAGE_CAPTURE)

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.extras?.get("data") as? Bitmap
        } else {
            null
        }
    }
}