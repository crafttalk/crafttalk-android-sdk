package com.crafttalk.chat.presentation.model

import android.text.SpannableString
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.TypeFile

data class FileModel(
    val url: String,
    val name: SpannableString,
    val size: Long? = null,
    val height: Int? = null,
    val width: Int? = null,
    val failLoading: Boolean = false,
    val type: TypeFile? = null,
    var typeDownloadProgress: TypeDownloadProgress = TypeDownloadProgress.NOT_DOWNLOADED
)