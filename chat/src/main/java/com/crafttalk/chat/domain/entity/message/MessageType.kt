package com.crafttalk.chat.domain.entity.message

enum class MessageType(val valueType: Int) {
    VISITOR_MESSAGE(1), //сообщение клиента
    INITIAL_MESSAGE(30), //сообщение, которое регулируется полем showInitialMessage
    OPERATOR_IS_TYPING(13), // оператор набирает сообщение
    OPERATOR_STOPPED_TYPING(14), // оператор закончил набирать сообщение
    READING_CONFIRMATION(4), // подтверждение прочтения сообщения клиентом
    RECEIVED_BY_MEDIATO(11), // подтверждение получения сообщения
    RECEIVED_BY_OPERATOR(12), // подтверждение прочтения сообщения клиента оператором
    UPDATE_DIALOG_SCORE(15), // обновление оценки оператора в диалоге
    CLOSE_DIALOG_INTENTION(17), // уведомление о намерении клиента завершить диалог
    FINISH_DIALOG(16), // завершение диалога
    UPDATE_NEGATIVE_REASON(19), // обновление негативной причины завершения диалога
    CLIENT_HOLD(23), // оператор отправил сообщение удержания (hold)   - ?
    TRANSFER_TO_OPERATOR(18), // техническое сообщение о подключении оператора
    MERGE_HISTORY(25), // синхронизация сообщений до и после перехода на новую авторизацию
    DEFAULT(-1),
    INFO_MESSAGE(-2);

    companion object {
        fun getMessageTypeByValueType(valueType: Int): MessageType {
            return when(valueType) {
                1 -> VISITOR_MESSAGE
                30 -> INITIAL_MESSAGE
                13 -> OPERATOR_IS_TYPING
                14 -> OPERATOR_STOPPED_TYPING
                4 -> READING_CONFIRMATION
                11 -> RECEIVED_BY_MEDIATO
                12 -> RECEIVED_BY_OPERATOR
                15 -> UPDATE_DIALOG_SCORE
                17 -> CLOSE_DIALOG_INTENTION
                16 -> FINISH_DIALOG
                19 -> UPDATE_NEGATIVE_REASON
                23 -> CLIENT_HOLD
                18 -> TRANSFER_TO_OPERATOR
                25 -> MERGE_HISTORY
                -2 -> INFO_MESSAGE
                else -> DEFAULT
            }
        }
    }

}