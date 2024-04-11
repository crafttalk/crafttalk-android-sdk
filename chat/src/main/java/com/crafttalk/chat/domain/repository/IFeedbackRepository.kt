package com.crafttalk.chat.domain.repository

interface IFeedbackRepository {
    fun giveFeedbackOnOperator(countStars: Int?, finishReason:String?, dialogID:String?)
}