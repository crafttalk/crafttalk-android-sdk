package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.api.rest.MessageApi
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.di.Base
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.di.Upload
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Upload
    @ChatScope
    @Provides
    fun provideRetrofitClientUpload(okHttpClient: OkHttpClient) = Retrofit
        .Builder()
        .baseUrl(ChatParams.urlChatHost!!)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .create()
        ))
        .build()

    @ChatScope
    @Provides
    fun provideMessageApi(@Base retrofit: Retrofit): MessageApi = retrofit.create(MessageApi::class.java)

    @ChatScope
    @Provides
    fun provideFileApi(@Upload retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

}