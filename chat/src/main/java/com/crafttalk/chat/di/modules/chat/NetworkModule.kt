package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.helper.network.TLSSocketFactory.Companion.enableTls
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.di.Upload
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Upload
    @ChatScope
    @Provides
    fun provideRetrofitClientUpload(certificate: CertificatePinner?): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .enableTls()
            .apply {
                certificate?.let { certificatePinner(it) }
                ChatParams.fileConnectTimeout?.let { connectTimeout(it, ChatParams.timeUnitTimeout) }
                ChatParams.fileReadTimeout?.let { readTimeout(it, ChatParams.timeUnitTimeout) }
                ChatParams.fileWriteTimeout?.let { writeTimeout(it, ChatParams.timeUnitTimeout) }
                ChatParams.fileCallTimeout?.let { callTimeout(it, ChatParams.timeUnitTimeout) }
            }
            .build()
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(ChatParams.urlUploadHost!!)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @ChatScope
    @Provides
    fun provideFileApi(@Upload retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

}