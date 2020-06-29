package com.crafttalk.chat.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object HashUtils {

    @Throws(NoSuchAlgorithmException::class)
    fun getHash(type: String, text: String): String {
        val messageDigest = MessageDigest.getInstance(type)
        messageDigest.update(text.toByteArray())
        val digest = messageDigest.digest()
        return encodeHexString(digest)
    }

    private fun encodeHexString(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }

}