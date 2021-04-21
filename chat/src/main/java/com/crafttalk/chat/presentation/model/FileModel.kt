package com.crafttalk.chat.presentation.model

import com.crafttalk.chat.domain.entity.file.TypeFile

data class FileModel(
    val url: String,
    val name: String,
    val size: Long,
    val height: Int = 0,
    val width: Int = 0,
    val failLoading: Boolean = false,
    val type: TypeFile? = null
)