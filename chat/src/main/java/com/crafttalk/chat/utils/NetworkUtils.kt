package com.crafttalk.chat.utils

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object NetworkUtils {

    fun isOnline(): Boolean {
        return try {
            val timeoutInMs = 1500
            val socket = Socket()
            val address = InetSocketAddress("8.8.8.8", 53)

            socket.connect(address, timeoutInMs)
            socket.close()

            true
        } catch (e: IOException) {
            false
        }
    }
}