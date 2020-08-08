package com.crafttalk.chat.domain.usecase.message

import com.crafttalk.chat.domain.repository.IMessageRepository

class UpdateSizeMessages constructor(
    private val messageRepository: IMessageRepository
) {

    operator fun invoke(idKey: Long, height: Int, width: Int, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.updateSizeMessage(idKey, height, width)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

}