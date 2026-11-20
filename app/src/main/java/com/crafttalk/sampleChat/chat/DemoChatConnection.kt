package com.crafttalk.sampleChat.chat

import android.content.Context
import android.widget.Toast
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.sampleChat.R
import java.net.URI
import java.net.URISyntaxException

internal fun initializeDemoChat(context: Context, channelUrl: String?, withForm: Boolean) {
    val uri = try {
        channelUrl?.trim()?.takeIf { it.isNotEmpty() }?.let(::URI)
    } catch (_: URISyntaxException) {
        Toast.makeText(context, "Некорректный URL", Toast.LENGTH_LONG).show()
        null
    }
    val channel = uri?.path?.substringAfterLast('/')
    val (scheme, host, namespace) = if (uri?.scheme != null && !uri.host.isNullOrEmpty() && !channel.isNullOrEmpty()) {
        val hostWithPort = if (uri.port == -1) uri.host else "${uri.host}:${uri.port}"
        Triple(uri.scheme, hostWithPort, channel)
    } else {
        Triple(context.getString(R.string.urlChatScheme), context.getString(R.string.urlChatHost),
            context.getString(R.string.urlChatNameSpace))
    }
    Chat.init(context, scheme, host, namespace,
        authType = if (withForm) AuthType.AUTH_WITH_FORM else AuthType.AUTH_WITHOUT_FORM,
        fileProviderAuthorities = context.getString(R.string.chat_file_provider_authorities))
    Chat.createSession()
}
