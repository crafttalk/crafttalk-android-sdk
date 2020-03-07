package com.crafttalk.chat.data.model

enum class MessageType(val valueType: Int) {
    VISITOR_MESSAGE(1), //сообщение клиента
    OPERATOR_IS_TYPING(13), // оператор набирает сообщение
    OPERATOR_STOPPED_TYPING(14), // оператор закончил набирать сообщение
    READING_CONFIRMATION(4), // подтверждение прочтения сообщения клиентом  - ?
    RECEIVED_BY_MEDIATO(11),
    RECEIVED_BY_OPERATOR(12), // подтверждение прочтения сообщения клиента оператором
    UPDATE_DIALOG_SCORE(15), // обновление оценки оператора в диалоге
    CLOSE_DIALOG_INTENTION(17), // уведомление о намерении клиента завершить диалог
    FINISH_DIALOG(16), // завершение диалога
    UPDATE_NEGATIVE_REASON(19), // обновление негативной причины завершения диалога
    CLIENT_HOLD(23); // оператор отправил сообщение удержания (hold)   - ?
}