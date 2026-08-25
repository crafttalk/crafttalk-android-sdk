package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.presentation.helper.extensions.createCorrectGlideUrl
import com.crafttalk.chat.utils.ChatParams
import java.net.URL
import kotlin.math.min

/**
 * Задержки перед повторными попытками получить медиафайл.
 * Сразу после отправки файл может быть временно недоступен, пока проходит проверку на стороне
 * сервера (например, антивирусной песочницей). Одной попытки в этом случае мало: размеры не будут
 * получены, в базу запишутся height/width = null, и сообщение навсегда останется "битым".
 */
private val MEDIA_SIZE_RETRY_DELAYS = longArrayOf(1_000L, 2_000L, 3_000L)

fun getSizeMediaFile(context: Context, url: String, resultSize: (height: Int?, width: Int?) -> Unit) {
    requestSizeMediaFile(context, url, 0, resultSize)
}

private fun requestSizeMediaFile(
    context: Context,
    url: String,
    attempt: Int,
    resultSize: (height: Int?, width: Int?) -> Unit
) {
    Glide.with(context.applicationContext)
        .asBitmap()
        .load(createCorrectGlideUrl(url))
        // Запрос служит только для замера размеров. Кэш не используется, чтобы не сохранить
        // ответ-заглушку, которую сервер отдаёт, пока файл ещё не прошёл проверку: такой ответ
        // Glide записал бы на диск по ключу url и потом отдавал бы его вместо картинки.
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                if (attempt < MEDIA_SIZE_RETRY_DELAYS.size) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        { requestSizeMediaFile(context, url, attempt + 1, resultSize) },
                        MEDIA_SIZE_RETRY_DELAYS[attempt]
                    )
                } else {
                    resultSize(null, null)
                }
            }
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                resultSize(resource.height, resource.width)
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}

/**
 * Блокирующая версия. Используется при синхронизации истории, где замер выполняется для каждого
 * сообщения подряд, поэтому по умолчанию повторные попытки выключены: к моменту загрузки истории
 * проверка файла на сервере давно завершена. Включать [withRetry] стоит только там, где файл
 * запрашивается сразу после его появления.
 */
fun getSizeMediaFile(context: Context, url: String, withRetry: Boolean = false): Pair<Int, Int>? {
    val attempts = if (withRetry) MEDIA_SIZE_RETRY_DELAYS.size + 1 else 1
    repeat(attempts) { attempt ->
        try {
            val resource = Glide.with(context.applicationContext)
                .asBitmap()
                .load(createCorrectGlideUrl(url))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .submit()
                .get()
            return Pair(resource.height, resource.width)
        } catch (ex: Exception) {
            if (attempt == attempts - 1) return null
            try {
                Thread.sleep(MEDIA_SIZE_RETRY_DELAYS[attempt])
            } catch (interruption: InterruptedException) {
                Thread.currentThread().interrupt()
                return null
            }
        }
    }
    return null
}

fun getWeightMediaFile(context: Context, url: String): Long? {
    return try {
        val weight = Glide.with(context)
            .asFile()
            .load(createCorrectGlideUrl(url))
            .submit()
            .get()
        weight.length()
    } catch (ex: Exception) {
        null
    }
}

fun getWeightFile(urlPath: String): Long? {
    val contentDispositionKey = "content-disposition"
    val template = "size="

    return try {
        val url = URL(urlPath)
        val urlConnection = url.openConnection()
        urlConnection.setRequestProperty("Cookie", "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}")
        urlConnection.setRequestProperty("ct-webchat-client-id", ChatParams.visitorUuid)
        urlConnection.connect()
        val size = urlConnection.contentLength

        if (size == -1) {
            val contentDisposition = urlConnection.getHeaderField(contentDispositionKey)
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