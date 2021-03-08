package com.crafttalk.chat.presentation.model

import com.crafttalk.chat.domain.entity.file.TypeFile

data class FileModel(
    val url: String,
    val name: String,
    val size: Long = 0,
    val height: Int = 0,
    val width: Int = 0,
    val type: TypeFile? = null
)