package com.crafttalk.chat.data.helper.network

import android.os.Build
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyStore
import javax.net.ssl.*

class TLSSocketFactory(
    private val sslSocketFactory: SSLSocketFactory
): SSLSocketFactory() {

    companion object {
        private val trustManager by lazy {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        @JvmStatic
        fun OkHttpClient.Builder.enableTls() = apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                try {
                    val sslContext = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName)
                    sslContext.init(null, arrayOf(trustManager), null)
                    sslSocketFactory(TLSSocketFactory(sslContext.socketFactory), trustManager)
                } catch (e: Exception) {
                    Log.e("ERROR_TLS", "Error while setting TLS 1.2 compatibility")
                }
            }
        }
    }

    private fun Socket.enableTLSOnSocket(): Socket {
        return (this as? SSLSocket)?.apply {
            enabledProtocols += TlsVersion.TLS_1_2.javaName
        } ?: this
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return sslSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return sslSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return sslSocketFactory.createSocket().enableTLSOnSocket()
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return sslSocketFactory.createSocket(s, host, port, autoClose).enableTLSOnSocket()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return sslSocketFactory.createSocket(host, port).enableTLSOnSocket()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return sslSocketFactory.createSocket(host, port, localHost, localPort).enableTLSOnSocket()
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return sslSocketFactory.createSocket(host, port).enableTLSOnSocket()
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return sslSocketFactory.createSocket(address, port, localAddress, localPort).enableTLSOnSocket()
    }

}