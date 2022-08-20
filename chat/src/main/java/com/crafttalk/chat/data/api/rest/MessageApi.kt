package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.message.NetworkBodySearch
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.entity.message.NetworkSearch
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.*

interface MessageApi {

    @GET("webhooks/webchat/{namespace}/message_feed")
    fun uploadMessages(
        @Path("namespace") clientId : String = ChatParams.urlChatNameSpace!!,
        @Query("visitor_uuid") uuid: String,
        @Query("last_timestamp") timestamp: Long,
        @Query("message_count") messageCount: Int = ChatParams.countDownloadedMessages,
        @Query("from_active_dialog") fromActiveDialog: Int = 0
    ) : Call<List<NetworkMessage>>

    @POST("webchat/search")
    fun searchMessages(
        @Body body: NetworkBodySearch
    ) : Call<NetworkSearch>
}