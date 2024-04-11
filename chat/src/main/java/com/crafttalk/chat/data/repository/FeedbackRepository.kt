package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.repository.IFeedbackRepository
import javax.inject.Inject

class FeedbackRepository
@Inject constructor(
    private val socketApi: SocketApi
) : IFeedbackRepository {

    override fun giveFeedbackOnOperator(countStars: Int?, finishReason:String?, dialogID:String?) {
        socketApi.giveFeedbackOnOperator(countStars, finishReason, dialogID)
    }

}