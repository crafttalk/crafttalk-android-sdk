package com.crafttalk.chat.presentation.helper.extensions

import android.view.View
import android.widget.ProgressBar
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress

fun ProgressBar.setProgressDownloadFile(typeDownloadProgress: TypeDownloadProgress) {
    visibility = when (typeDownloadProgress) {
        TypeDownloadProgress.NOT_DOWNLOADED, TypeDownloadProgress.DOWNLOADED -> View.INVISIBLE
        TypeDownloadProgress.DOWNLOADING -> View.VISIBLE
    }
}