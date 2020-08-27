package com.crafttalk.chat.di.modules

import com.crafttalk.chat.data.ApiParams.HOST
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.repository.DataRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofitClient(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideFileApi(retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

    @Singleton
    @Provides
    fun provideSocketApi(dataRepository: DataRepository, gson: Gson) = SocketApi(
        dataRepository,
        gson
    )

}