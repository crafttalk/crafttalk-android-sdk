package com.crafttalk.chat.di.modules.init

import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.helper.network.TLSSocketFactory.Companion.enableTls
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.di.Notification
import com.crafttalk.chat.utils.ChatParams.urlSocketHost
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Notification
    @Singleton
    @Provides
    fun provideRetrofitClientNotification(gson: Gson): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .enableTls()
            .build()
        return Retrofit.Builder()
            .baseUrl(urlSocketHost!!)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideNotificationApi(@Notification retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Singleton
    @Provides
    fun provideSocketApi(messagesDao: MessagesDao, gson: Gson) = SocketApi(
        messagesDao,
        gson
    )

}