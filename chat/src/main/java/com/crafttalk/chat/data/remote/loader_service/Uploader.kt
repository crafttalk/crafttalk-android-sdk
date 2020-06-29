package com.crafttalk.chat.data.remote.loader_service

import com.crafttalk.chat.data.remote.loader_service.preparers.FilePreparer
import com.crafttalk.chat.data.remote.loader_service.preparers.ImagePreparer
import com.crafttalk.chat.utils.ConstantsUtils.URL_UPLOAD_HOST
import com.crafttalk.chat.utils.ConstantsUtils.URL_UPLOAD_NAMESPACE
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object Uploader {

    private var retrofit: Retrofit? = null
    private var classService: Class<Any>? = null
    private var expressFrontendHost: String = URL_UPLOAD_HOST
    var clientId: String = URL_UPLOAD_NAMESPACE
    val imagePreparer by lazy {
        ImagePreparer()
    }
    val filePreparer by lazy {
        FilePreparer()
    }

    fun service() = (retrofit ?: buildDefaultRetrofit(expressFrontendHost)).create(
        classService ?: getDefaultClassService()
    )

    fun setDefaultRetrofit(customRetrofit: Retrofit): Uploader {
        retrofit = customRetrofit
        return this
    }

    fun setDefaultRetrofit(customRetrofit: () -> Retrofit): Uploader {
        retrofit = customRetrofit.invoke()
        return this
    }

    fun setDefaultClassService(customClassService: Class<Any>): Uploader {
        classService = customClassService
        return this
    }

    fun setDefaultClassService(customClassService: () -> Class<Any>): Uploader {
        classService = customClassService.invoke()
        return this
    }

    fun setDefaultHostServerName(hostServerName: String): Uploader {
        expressFrontendHost = hostServerName
        return this
    }

    fun setDefaultHostServerName(hostServerName: () -> String): Uploader {
        expressFrontendHost = hostServerName.invoke()
        return this
    }

    fun setDefaultNamespaceClientInSocket(namespaceClientInSocket: String): Uploader {
        clientId = namespaceClientInSocket
        return this
    }

    fun setDefaultNamespaceClientInSocket(namespaceClientInSocket: () -> String): Uploader {
        clientId = namespaceClientInSocket.invoke()
        return this
    }

    private fun buildDefaultRetrofit(url: String): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
//            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    private fun getDefaultClassService() = LoaderInterface::class.java

}