package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.presentation.helper.extensions.createCorrectGlideUrl
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.ConstantsUtils.TAG_FILE_SIZE
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

private const val CONTENT_DISPOSITION_KEY = "content-disposition"
private const val CONTENT_LENGTH_KEY = "content-length"
private const val CONTENT_TYPE_KEY = "content-type"

/**
 * Типы ответов, которыми сервер сообщает об ошибке или о том, что файл ещё не готов.
 * Настоящий документ с таким типом прийти не может, а json пользователь отправляет крайне редко --
 * в этом случае просто не покажем размер.
 */
private val STUB_CONTENT_TYPES = listOf("text/html", "application/json")

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

/**
 * Размер файла по его url.
 *
 * Сервер не всегда отдаёт по этому адресу сам файл: он может ответить ошибкой или короткой
 * заглушкой, пока файл ещё проходит проверку. Размер такого ответа не имеет отношения к размеру
 * файла, поэтому берём его только тогда, когда ответ действительно похож на файл. Иначе лучше
 * не показать размер вовсе, чем записать в базу неверный: там он останется навсегда.
 */
fun getWeightFile(urlPath: String): Long? {
    var connection: HttpURLConnection? = null
    return try {
        connection = (URL(urlPath).openConnection() as? HttpURLConnection ?: return null).apply {
            setRequestProperty("Cookie", "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}")
            setRequestProperty("ct-webchat-client-id", ChatParams.visitorUuid)
            // У сжатого ответа content-length -- это размер после сжатия, а не размер файла
            setRequestProperty("Accept-Encoding", "identity")
            connect()
        }

        // Размер, объявленный самим сервером в content-disposition, надёжнее content-length:
        // он относится к файлу, а не к тому, что сервер отдал в этот раз.
        val contentDisposition = connection.getHeaderField(CONTENT_DISPOSITION_KEY)
        parseSizeFromContentDisposition(contentDisposition)?.let { return it }

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.w(TAG_FILE_SIZE, "Response code $responseCode, url - $urlPath")
            return null
        }
        val contentType = connection.getHeaderField(CONTENT_TYPE_KEY)
        if (STUB_CONTENT_TYPES.any { contentType?.startsWith(it, ignoreCase = true) == true }) {
            Log.w(TAG_FILE_SIZE, "Got $contentType instead of file, url - $urlPath")
            return null
        }
        val size = connection.getHeaderField(CONTENT_LENGTH_KEY)?.trim()?.toLongOrNull()
        if (size == null || size <= 0L) {
            Log.w(TAG_FILE_SIZE, "Server has not reported size, url - $urlPath")
            null
        } else {
            size
        }
    } catch (ex: Exception) {
        Log.w(TAG_FILE_SIZE, "Can't get file size, url - $urlPath", ex)
        null
    } finally {
        connection?.disconnect()
    }
}

/**
 * Вытаскивает размер файла из content-disposition, где он приходит в виде "...size=12345...".
 */
private fun parseSizeFromContentDisposition(contentDisposition: String?): Long? {
    val template = "size="
    val startIndex = contentDisposition?.indexOf(template)
        ?.takeIf { it != -1 }
        ?.plus(template.length)
        ?: return null
    val indexEndComma = contentDisposition.indexOf(",", startIndex)
    val indexEndBracket = contentDisposition.indexOf("]", startIndex)
    val endIndex = when {
        indexEndComma != -1 && indexEndBracket != -1 -> min(indexEndComma, indexEndBracket)
        indexEndComma != -1 -> indexEndComma
        indexEndBracket != -1 -> indexEndBracket
        else -> contentDisposition.length
    }
    return contentDisposition.substring(startIndex, endIndex).trim().toLongOrNull()?.takeIf { it > 0L }
}