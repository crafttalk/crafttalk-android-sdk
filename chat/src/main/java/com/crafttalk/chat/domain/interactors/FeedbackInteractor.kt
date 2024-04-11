package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IFeedbackRepository
import javax.inject.Inject

class FeedbackInteractor
@Inject constructor(
    private val feedbackRepository: IFeedbackRepository
) {

    fun giveFeedbackOnOperator(countStars: Int?, finishReason:String?, dialogID:String?) {
        feedbackRepository.giveFeedbackOnOperator(countStars, finishReason, dialogID)
    }

}