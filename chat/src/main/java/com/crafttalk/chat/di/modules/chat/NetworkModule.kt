package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.di.Upload
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Upload
    @ChatScope
    @Provides
    fun provideRetrofitClientUpload(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(ChatParams.urlUploadHost!!)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @ChatScope
    @Provides
    fun provideFileApi(@Upload retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

}