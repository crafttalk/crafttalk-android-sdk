package com.crafttalk.chat

import com.crafttalk.chat.data.helper.network.WebchatClientHash
import com.crafttalk.chat.data.helper.network.InvalidWebchatClientHash
import com.crafttalk.chat.di.modules.chat.NetworkModule
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.NetworkBodyStructureUploadFile
import com.crafttalk.chat.domain.entity.message.NetworkBodySearch
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.presentation.helper.extensions.createCorrectGlideUrl
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue

class WebchatClientHashTest {
    private val server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
    private val headers = LinkedBlockingQueue<List<String>>()
    private val host = "127.0.0.1:${server.localPort}"

    @Before fun start() {
        ChatParams.urlChatScheme = "http"
        ChatParams.urlChatHost = host
        ChatParams.urlChatNameSpace = "test"
        ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", "signed")
        thread(isDaemon = true) {
            while (!server.isClosed) {
                val client = try { server.accept() } catch (e: SocketException) {
                    if (server.isClosed) return@thread else throw e
                }
                client.use { socket ->
                    val input = socket.getInputStream().bufferedReader(Charsets.US_ASCII)
                    val lines = generateSequence { input.readLine() }.takeWhile { it.isNotEmpty() }.toList()
                    if (lines.isEmpty()) return@use
                    val hash = lines.filter { it.startsWith("${WebchatClientHash.HEADER}:", true) }
                    headers.add(hash.map { it.substringAfter(':').removePrefix(" ") })
                    val length = lines.firstOrNull { it.startsWith("Content-Length:", true) }
                        ?.substringAfter(':')?.trim()?.toInt() ?: 0
                    repeat(length) { input.read() }
                    socket.getOutputStream().write("HTTP/1.1 204 No Content\r\nConnection: close\r\n\r\n".toByteArray())
                }
            }
        }
    }

    @After fun stop() {
        server.close()
        ChatParams.visitorHeaders = ChatParams.VisitorHeaders("", null)
        ChatParams.urlChatScheme = null
        ChatParams.urlChatHost = null
        ChatParams.urlChatNameSpace = null
    }

    @Test fun sendsHashOnSearchAndBothUploadsOnly() {
        val module = NetworkModule()
        val client = module.provideRetrofitClientUpload(OkHttpClient())
        val messages = module.provideMessageApi(client)
        val files = module.provideFileApi(client)
        val search = NetworkBodySearch("visitor", "test")
        messages.searchMessages(body = search).execute()
        files.uploadFile(networkBody = NetworkBodyStructureUploadFile("file", "YQ==", "visitor")).execute()
        files.uploadFile(fileName = "file".toRequestBody(), uuid = "visitor".toRequestBody(),
            fileB64 = MultipartBody.Part.createFormData("file", "file", "a".toRequestBody())).execute()
        messages.uploadMessages(uuid = "visitor", timestamp = 0).execute()
        ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", null)
        messages.searchMessages(body = search).execute()
        assertEquals(listOf(listOf("signed"), listOf("signed"), listOf("signed"), emptyList(), emptyList()), headers.toList())
    }

    @Test fun fileHashIsLimitedToWebchatFilesOnThisServerAndChannel() {
        val file = "http://$host/webchat/file/access/test/bucket/file"
        val connection = URL(file).openConnection() as HttpURLConnection
        try {
            WebchatClientHash.applyToFileConnection(connection, ChatParams.visitorHeaders)
            assertEquals(204, connection.responseCode)
            assertEquals(listOf("signed"), headers.remove())
            assertFalse(connection.instanceFollowRedirects)
        } finally {
            connection.disconnect()
        }
        assertEquals("signed", fileHash(file.replace("/access/", "/access//")))
        assertNull(fileHash(file.replace("127.0.0.1", "localhost")))
        assertNull(fileHash(file.replace("/test/", "/other/")))
        assertNull(fileHash("http://$host/avatar.png"))
        ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", null)
        assertNull(fileHash(file))
    }

    private fun fileHash(url: String) = WebchatClientHash.forFile(url, ChatParams.visitorHeaders)

    @Test fun distinguishesAbsentAndInvalidHashesOnTheWire() {
        val module = NetworkModule()
        val client = module.provideRetrofitClientUpload(OkHttpClient())
        val messages = module.provideMessageApi(client)
        val files = module.provideFileApi(client)
        val search = NetworkBodySearch("visitor", "test")
        for (absent in listOf(null, "", "   ", "null", "undefined")) {
            ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", absent)
            assertEquals(204, messages.searchMessages(body = search).execute().code())
            assertEquals(emptyList<String>(), headers.remove())
        }
        ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", "signed  value")
        assertEquals(204, messages.searchMessages(body = search).execute().code())
        assertEquals(listOf("signed  value"), headers.remove())

        val file = "http://$host/webchat/file/access/test/bucket/file"
        for (invalid in listOf(" signed", "signed ", "signed\r\nvalue", "signed\u0000", "подпись")) {
            ChatParams.visitorHeaders = ChatParams.VisitorHeaders("visitor", invalid)
            val error = assertThrows(InvalidWebchatClientHash::class.java) {
                messages.searchMessages(body = search).execute()
            }
            assertFalse(error.message.orEmpty().contains(invalid))
            val connection = URL(file).openConnection() as HttpURLConnection
            try {
                assertThrows(InvalidWebchatClientHash::class.java) {
                    WebchatClientHash.applyToFileConnection(connection, ChatParams.visitorHeaders)
                }
            } finally {
                connection.disconnect()
            }
            assertNull(createCorrectGlideUrl(file))
            assertNull(fileHash(file.replace("/test/", "/other/")))
        }
        assertThrows(InvalidWebchatClientHash::class.java) {
            files.uploadFile(networkBody = NetworkBodyStructureUploadFile("file", "YQ==", "visitor")).execute()
        }
        assertThrows(InvalidWebchatClientHash::class.java) {
            files.uploadFile(fileName = "file".toRequestBody(), uuid = "visitor".toRequestBody(),
                fileB64 = MultipartBody.Part.createFormData("file", "file", "a".toRequestBody())).execute()
        }
        assertEquals(204, messages.uploadMessages(uuid = "visitor", timestamp = 0).execute().code())
        assertEquals(listOf(emptyList<String>()), headers.toList())
    }

    @Test fun usesHashFromTheVisitorsAuthorizationFields() {
        val visitor = Visitor("visitor", "token", "", "", null, null, null, null, "signed")
        assertEquals("signed", Visitor.authorizationHash(visitor))
        visitor.addNewField("hash", "override")
        assertEquals("override", Visitor.authorizationHash(visitor))
        visitor.addNewField("hash", "")
        assertEquals("", Visitor.authorizationHash(visitor))
        val restored = Gson().fromJson("""{"uuid":"visitor","hash":"signed"}""", Visitor::class.java)
        assertEquals("signed", Visitor.authorizationHash(restored))
    }
}
