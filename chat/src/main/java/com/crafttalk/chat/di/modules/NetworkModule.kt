package com.crafttalk.chat.di.modules

import com.crafttalk.chat.data.ApiParams.NOTIFICATION_HOST
import com.crafttalk.chat.data.ApiParams.UPLOAD_HOST
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.repository.DataRepository
import com.crafttalk.chat.di.Notification
import com.crafttalk.chat.di.Upload
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Upload
    @Singleton
    @Provides
    fun provideRetrofitClientUpload(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UPLOAD_HOST)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Notification
    @Singleton
    @Provides
    fun provideRetrofitClientNotification(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NOTIFICATION_HOST)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideFileApi(@Upload retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

    @Singleton
    @Provides
    fun provideNotificationApi(@Notification retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Singleton
    @Provides
    fun provideSocketApi(dataRepository: DataRepository, gson: Gson) = SocketApi(
        dataRepository,
        gson
    )

}