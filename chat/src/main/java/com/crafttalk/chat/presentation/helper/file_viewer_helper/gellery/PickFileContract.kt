package com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.model.TypeMultiple

class PickFileContract : ActivityResultContract<Pair<TypeFile, TypeMultiple>, List<Uri>>() {

    override fun createIntent(context: Context, input: Pair<TypeFile, TypeMultiple>): Intent =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            when (input.second) {
                TypeMultiple.SINGLE -> putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                TypeMultiple.MULTIPLE -> putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            when (input.first) {
                TypeFile.FILE -> putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                TypeFile.IMAGE -> {}
                TypeFile.GIF -> {}
            }
            type = input.first.value
        }


    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
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
        return imagesUriList
    }

}