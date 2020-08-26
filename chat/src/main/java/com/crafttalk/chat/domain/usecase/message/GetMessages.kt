package com.crafttalk.chat.domain.usecase.message

import androidx.lifecycle.LiveData
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class GetMessages
@Inject constructor(
    private val messageRepository: IMessageRepository
) {

    operator fun invoke(): LiveData<List<Message>> = messageRepository.getMessagesList()

}