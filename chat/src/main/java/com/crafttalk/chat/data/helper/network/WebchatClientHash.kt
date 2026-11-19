package com.crafttalk.chat.data.helper.network

import com.crafttalk.chat.utils.ChatParams
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import java.net.ProtocolException

internal class InvalidWebchatClientHash : ProtocolException("Invalid webchat client hash for HTTP header")

internal object WebchatClientHash : Interceptor {
    const val HEADER = "CT-Webchat-Client-Hash"

    // This client serves search, uploads and the message feed. Only the first two require the hash.
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val (uuid, hash) = ChatParams.visitorHeaders
        val namespace = ChatParams.urlChatNameSpace
        val original = chain.call().request()
        if (hash == null || namespace == null || request.url != original.url ||
            request.method != "POST" || request.header("ct-webchat-client-id") != uuid ||
            original.header("Cookie") != "webchat-$namespace-uuid=$uuid" || !isChatServer(request.url)) return chain.proceed(request)

        val path = request.url.pathSegments
        val requiresHash = path == listOf("webchat", "search") || path == listOf("webchat", namespace, "upload-file")
        val value = if (requiresHash) headerValue(hash) else null
        return chain.proceed(if (value == null) request else request.newBuilder().header(HEADER, value).build())
    }

    fun forFile(url: String, headers: ChatParams.VisitorHeaders): String? {
        val hash = headers.hash ?: return null
        val target = url.toHttpUrlOrNull() ?: return null
        if ('\\' in url || !isChatServer(target)) return null
        val path = target.pathSegments
        if (path.size < 6 || path[0] != "webchat" || path[1] != "file" || path[2].isEmpty()) return null
        // Existing webchat deployments also produce file URLs with a double slash before the channel.
        val channel = if (path[3].isEmpty()) 4 else 3
        if (path.size < channel + 3 || path[channel] != ChatParams.urlChatNameSpace) return null
        return headerValue(hash)
    }

    private fun headerValue(hash: String): String? {
        if (hash.all { it == ' ' || it == '\t' } || hash == "null" || hash == "undefined") return null
        // OkHttp trims surrounding whitespace. Sending a changed credential would break socket/HTTP parity.
        if (hash.first().isWhitespace() || hash.last().isWhitespace() ||
            hash.any { it !in ' '..'~' && it != '\t' }) throw InvalidWebchatClientHash()
        return hash
    }

    fun applyToFileConnection(connection: HttpURLConnection, headers: ChatParams.VisitorHeaders) {
        forFile(connection.url.toString(), headers)?.let {
            connection.instanceFollowRedirects = false
            connection.setRequestProperty(HEADER, it)
        }
    }

    private fun isChatServer(url: HttpUrl): Boolean {
        val server = "${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}".toHttpUrlOrNull() ?: return false
        return url.scheme == server.scheme && url.host == server.host && url.port == server.port &&
            url.encodedUsername.isEmpty() && url.encodedPassword.isEmpty()
    }
}
