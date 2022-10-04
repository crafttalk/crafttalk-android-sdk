package com.crafttalk.chat.di.modules.init

import android.content.Context
import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.data.api.rest.PersonApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.helper.network.CustomCookieJar
import com.crafttalk.chat.data.helper.network.TLSSocketFactory.Companion.enableTls
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.di.Base
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideCertificatePinner(): CertificatePinner? {
        return ChatParams.certificatePinning?.let {
            CertificatePinner.Builder()
                .add(ChatParams.urlChatHost!!, it)
                .build()
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(certificate: CertificatePinner?) = OkHttpClient
        .Builder()
        .enableTls()
        .apply {
            certificate?.let { certificatePinner(it) }
            ChatParams.fileConnectTimeout?.let { connectTimeout(it, ChatParams.timeUnitTimeout) }
            ChatParams.fileReadTimeout?.let { readTimeout(it, ChatParams.timeUnitTimeout) }
            ChatParams.fileWriteTimeout?.let { writeTimeout(it, ChatParams.timeUnitTimeout) }
            ChatParams.fileCallTimeout?.let { callTimeout(it, ChatParams.timeUnitTimeout) }
        }
        .cookieJar(CustomCookieJar())
        .build()

    @Base
    @Singleton
    @Provides
    fun provideBaseRetrofitClient(okHttpClient: OkHttpClient, gson: Gson) = Retrofit.Builder()
        .baseUrl("${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Singleton
    @Provides
    fun provideNotificationApi(@Base retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Singleton
    @Provides
    fun providePersonApi(@Base retrofit: Retrofit): PersonApi = retrofit.create(PersonApi::class.java)

    @Singleton
    @Provides
    fun provideSocketApi(okHttpClient: OkHttpClient, messagesDao: MessagesDao, gson: Gson, context: Context) = SocketApi(
        okHttpClient,
        messagesDao,
        gson,
        context
    )

}