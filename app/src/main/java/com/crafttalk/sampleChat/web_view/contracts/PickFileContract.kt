package com.crafttalk.sampleChat.web_view.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

data class PickFileModel(
    val typeFile: String,
    val isMultiple: Boolean
)

class PickFileContract : ActivityResultContract<PickFileModel, Array<Uri?>>() {

    override fun createIntent(context: Context, input: PickFileModel): Intent =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, input.isMultiple)
            if (input.typeFile == "application/*") {
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            type = input.typeFile
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Array<Uri?> {
        val imagesUriList = mutableListOf<Uri>()
        if (resultCode == Activity.RESULT_OK) {
            if (intent?.clipData != null) {
                val clipData = intent.clipData
                val itemCount = clipData?.itemCount!!
                if (itemCount > 0) {
                    for (i in 0 until itemCount) {
                        val item = clipData.getItemAt(i)
                        imagesUriList.add(item.uri)
                    }
                }
            } else if (intent?.data != null) {
                imagesUriList.add(intent.data!!)
            }
        }
        return imagesUriList.toTypedArray()
    }
}