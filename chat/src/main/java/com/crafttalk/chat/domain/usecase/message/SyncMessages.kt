package com.crafttalk.chat.domain.usecase.message

import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class SyncMessages
@Inject constructor(
    private val messageRepository: IMessageRepository
) {

    suspend operator fun invoke(timestamp: Long = 0, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.syncMessages(timestamp)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

}