package com.crafttalk.chat.data.remote.loader_service

import com.crafttalk.chat.data.remote.loader_service.preparers.FilePreparer
import com.crafttalk.chat.data.remote.loader_service.preparers.ImagePreparer
import com.crafttalk.chat.utils.ConstantsUtils.NAMESPACE
import com.crafttalk.chat.utils.ConstantsUtils.SOCKET_URL
import retrofit2.Retrofit

object Uploader {

    private var retrofit: Retrofit? = null
    private var classService: Class<Any>? = null
    private var expressFrontendHost: String = SOCKET_URL
    var clientId: String = NAMESPACE
    val imagePreparer by lazy {
        ImagePreparer()
    }
    val filePreparer by lazy {
        FilePreparer()
    }

    fun service() = retrofit ?: buildDefaultRetrofit(expressFrontendHost).create(
        classService ?: getDefaultClassService()
    )

    fun setDefaultRetrofit(customRetrofit: Retrofit) {
        retrofit = customRetrofit
    }

    fun setDefaultRetrofit(customRetrofit: () -> Retrofit) {
        retrofit = customRetrofit.invoke()
    }

    fun setDefaultClassService(customClassService: Class<Any>) {
        classService = customClassService
    }

    fun setDefaultClassService(customClassService: () -> Class<Any>) {
        classService = customClassService.invoke()
    }

    fun setDefaultHostServerName(hostServerName: String) {
        expressFrontendHost = hostServerName
    }

    fun setDefaultHostServerName(hostServerName: () -> String) {
        expressFrontendHost = hostServerName.invoke()
    }

    fun setDefaultNamespaceClientInSocket(namespaceClientInSocket: String) {
        clientId = namespaceClientInSocket
    }

    fun setDefaultNamespaceClientInSocket(namespaceClientInSocket: () -> String) {
        clientId = namespaceClientInSocket.invoke()
    }

    private fun buildDefaultRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .build()
    }

    private fun getDefaultClassService() = LoaderInterface::class.java

}
