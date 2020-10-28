package com.crafttalk.chat.domain.interactor

import com.crafttalk.chat.domain.repository.INotificationRepository
import javax.inject.Inject

class NotificationInteractor
@Inject constructor(
    private val notificationRepository: INotificationRepository
) {

    suspend fun subscribeNotification(uuid: String) {
        notificationRepository.subscribe(uuid)
    }

    suspend fun unsubscribeNotification(uuid: String) {
        notificationRepository.unSubscribe(uuid)
    }

}