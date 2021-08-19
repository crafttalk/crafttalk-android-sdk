package com.crafttalk.chat.domain.repository

interface IFeedbackRepository {
    fun giveFeedbackOnOperator(countStars: Int)
}