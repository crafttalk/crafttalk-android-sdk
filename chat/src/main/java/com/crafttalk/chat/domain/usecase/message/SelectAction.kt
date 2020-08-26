package com.crafttalk.chat.domain.usecase.message

import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class SelectAction
@Inject constructor(
    private val messageRepository: IMessageRepository
) {

    suspend operator fun invoke(actionId: String, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.selectAction(actionId)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

}