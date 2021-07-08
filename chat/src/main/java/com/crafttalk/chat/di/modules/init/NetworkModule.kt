package com.crafttalk.chat.di.modules.init

import android.content.Context
import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.data.api.rest.PersonApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.helper.network.TLSSocketFactory.Companion.enableTls
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.TransactionMessageDao
import com.crafttalk.chat.di.Notification
import com.crafttalk.chat.utils.ChatParams.certificatePinning
import com.crafttalk.chat.utils.ChatParams.urlSocketHost
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
        return certificatePinning?.let {
            CertificatePinner.Builder()
                .add(urlSocketHost!!.substringAfter("://"), it)
                .build()
        }
    }

    @Notification
    @Singleton
    @Provides
    fun provideRetrofitClientNotification(gson: Gson, certificate: CertificatePinner?): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .enableTls()
            .apply { certificate?.let { certificatePinner(it) } }
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
    fun providePersonApi(@Notification retrofit: Retrofit): PersonApi = retrofit.create(PersonApi::class.java)

    @Singleton
    @Provides
    fun provideSocketApi(messagesDao: MessagesDao, transactionMessageDao: TransactionMessageDao, gson: Gson, context: Context) = SocketApi(
        messagesDao,
        transactionMessageDao,
        gson,
        context
    )

}