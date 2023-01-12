package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.net.URL
import kotlin.math.min

fun getSizeMediaFile(context: Context, url: String, resultSize: (height: Int?, width: Int?) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                resultSize(null, null)
            }
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                resultSize(resource.height, resource.width)
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}

fun getSizeMediaFile(context: Context, url: String): Pair<Int, Int>? {
    return try {
        val resource = Glide.with(context)
            .asBitmap()
            .load(url)
            .submit()
            .get()
        Pair(resource.height, resource.width)
    } catch (ex: Exception) {
        null
    }
}

fun getWeightMediaFile(context: Context, url: String): Long? {
    return try {
        val weight = Glide.with(context)
            .asFile()
            .load(url)
            .submit()
            .get()
        weight.length()
    } catch (ex: Exception) {
        null
    }
}

fun getWeightFile(urlPath: String): Long? {
    val CONTENT_DISPOSITION = "content-disposition"
    val template = "size="

    return try {
        val url = URL(urlPath)
        val urlConnection = url.openConnection()
        urlConnection.connect()
        val size = urlConnection.contentLength

        if (size == -1) {
            val contentDisposition = urlConnection.getHeaderField(CONTENT_DISPOSITION)
            if (contentDisposition == null) {
                null
            } else {
                val startIndex = contentDisposition.indexOf(template) + template.length
                val indexEndComma = contentDisposition.indexOf(",", startIndex)
                val indexEndBracket = contentDisposition.indexOf("]", startIndex)
                val alternativeSize = (when {
                    startIndex != -1 && indexEndComma != -1 && indexEndBracket != -1 -> contentDisposition.substring(startIndex, min(indexEndComma, indexEndBracket))
                    startIndex != -1 && indexEndComma != -1 && indexEndBracket == -1 -> contentDisposition.substring(startIndex, indexEndComma)
                    startIndex != -1 && indexEndComma == -1 && indexEndBracket != -1 -> contentDisposition.substring(startIndex, indexEndBracket)
                    startIndex != -1 && indexEndComma == -1 && indexEndBracket == -1 -> contentDisposition.substring(startIndex)
                    else -> null
                })?.toLong()
                if (alternativeSize == 0L) {
                    null
                } else {
                    alternativeSize
                }
            }
        } else {
            if (size == 0) {
                null
            } else {
                size.toLong()
            }
        }
    } catch (ex: Exception) {
        null
    }
}