package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.INotificationRepository
import javax.inject.Inject

class NotificationInteractor
@Inject constructor(
    private val notificationRepository: INotificationRepository
) {

    fun subscribeNotification(uuid: String) {
        notificationRepository.subscribe(uuid)
    }

    fun unsubscribeNotification(uuid: String) {
        notificationRepository.unSubscribe(uuid)
    }

}