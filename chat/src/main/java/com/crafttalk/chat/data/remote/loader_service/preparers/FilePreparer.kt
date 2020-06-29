package com.crafttalk.chat.data.remote.loader_service.preparers

import android.util.Base64
import java.io.InputStream


class FilePreparer {

    fun convertFileToBase64(inputStream: InputStream): String {
        return Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
    }

}