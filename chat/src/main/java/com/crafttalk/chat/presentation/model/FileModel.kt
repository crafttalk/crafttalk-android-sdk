package com.crafttalk.chat.presentation.model

import com.crafttalk.chat.domain.entity.file.TypeFile

data class FileModel(
    val url: String,
    val name: String,
    val height: Int,
    val width: Int,
    val type: TypeFile? = null
)