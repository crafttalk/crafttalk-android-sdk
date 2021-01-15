package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.INotificationRepository
import javax.inject.Inject

class NotificationInteractor
@Inject constructor(
    private val notificationRepository: INotificationRepository,
    private val visitorInteractor: VisitorInteractor
) {

    fun subscribeNotification() {
        visitorInteractor.getVisitor()?.let {
            notificationRepository.subscribe(it.uuid)
        }
    }

    fun unsubscribeNotification() {
        visitorInteractor.getVisitor()?.let {
            notificationRepository.unSubscribe(it.uuid)
        }
    }

}