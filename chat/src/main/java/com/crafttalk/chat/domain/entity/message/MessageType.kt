package com.crafttalk.chat.domain.entity.message

/**
 *      Коды сообщений
 *      -2 -> INFO_MESSAGE
 *      0 -> INITIAL
 *      1 -> MESSAGE
 *      2 -> SENT_CONFIRMATION
 *      4 -> READING_CONFIRMATION
 *      5 -> FAILED_CONFIRMATION
 *      8 -> DELETED
 *      9 -> PHONE_NUMBER
 *      10 -> LOCATION
 *      11 -> RECEIVED_BY_MEDIATOR
 *      12 -> RECEIVED_BY_OPERATOR
 *      13 -> OPERATOR_IS_TYPING
 *      14 -> OPERATOR_STOPPED_TYPING
 *      15 -> UPDATE_DIALOG_SCORE
 *      16 -> FINISH_DIALOG
 *      17 -> CLOSE_DIALOG_INTENTION
 *      18 -> CONNECTED_OPERATOR
 *      19 -> UPDATE_NEGATIVE_REASON
 *      20 -> AUTO_GREETING
 *      21 -> UPDATE_DIALOG_NEGATIVE_REASON
 *      22 -> UPDATE_USER_DATA
 *      23 -> CLIENT_HOLD
 *      24 -> UPDATE_DIALOG_USEFULNESS
 *      25 -> USER_WAS_MERGED
 *      26 -> USER_WAS_AUTHORIZED
 *      27 -> USER_WAS_DEAUTHORIZED
 *      28 -> USER_MESSAGE_SENT
 *      29 -> USER_STOPPED_TYPING
 *      30 -> INITIAL_MESSAGE
 */
enum class MessageType(val valueType: Int) {
    INITIAL(0), //Стартовое  сообщение,  отправляемое  каналами. Например, /start в Telegram
    MESSAGE(1), //Обычное  отображаемое  сообщение,  отправленное пользователем или оператором
    SENT_CONFIRMATION(2), //Служебное  сообщение,  обозначающее  успешную отправку исходящего сообщения в канал
    READING_CONFIRMATION(4), // Служебное  сообщение,  обозначающее  прочтение исходящего сообщения в канале
    FAILED_CONFIRMATION(5), // Служебное  сообщение, обозначающее  неудачную  отправку  исходящего сообщения в канал
    DELETED(8), //Служебное  сообщение,  обозначающее  что  ParentMessageId  был  удалён  в канале.
    PHONE_NUMBER(9), //Служебное  сообщение, обозначающее что из канала поступил номер телефона
    LOCATION(10), // Служебное  сообщение,  обозначающее  что  из  канала  поступило местоположение
    RECEIVED_BY_MEDIATOR(11), // Служебное  сообщение, обозначающее  что  бекенд  принял  входящее сообщение
    RECEIVED_BY_OPERATOR(12), // Служебное  сообщение,  обозначающее  что  оператор  прочитал  входящее сообщение
    OPERATOR_IS_TYPING(13), // Служебное  сообщение, обозначающее что оператор начал печатать текст
    OPERATOR_STOPPED_TYPING(14), // Служебное  сообщение, обозначающее что оператор прекратил печатать текст
    UPDATE_DIALOG_SCORE(15), // Служебное  сообщение, обозначающее изменение оценки диалога
    FINISH_DIALOG(16), // Служебное  сообщение, представляющее  команду  бекенду  для  завершения диалога
    CLOSE_DIALOG_INTENTION(17), // Служебное  сообщение, обозначающее намерение клиента завершить диалог
    CONNECTED_OPERATOR(18), // Служебное  сообщение, обозначающее что оператор подключился к диалогу
    UPDATE_NEGATIVE_REASON(19), // Служебное  сообщение, обозначающее что диалог завершен
    AUTO_GREETING(20), //Служебное  сообщение, обозначающее автоприветствие
    UPDATE_DIALOG_NEGATIVE_REASON(21), // Служебное  сообщение, обозначающее  изменение  недовольства  клиента  в диалоге
    UPDATE_USER_DATA(22), //Служебное  сообщение, обозначающее  принудительное  обновление  данных клиента
    CLIENT_HOLD(23), // Служебное  сообщение, обозначающее уведомление вебчата о постановке на hold
    UPDATE_DIALOG_USEFULNESS(24), // Служебное  сообщение,  обозначающее  обновление  "полезности"  диалога (было ли общение в диалоге)
    USER_WAS_MERGED(25), // синхронизация сообщений до и после перехода на новую авторизацию Служебное  сообщение,  обозначающее  что  было  произведено  слияние пользователя с другим пользователем
    USER_WAS_AUTHORIZED(26), //Служебное  сообщение,  обозначающее  что  пользователь  был  успешно  авторизован
    USER_WAS_DEAUTHORIZED(27), // Служебное  сообщение, обозначающее  что  пользователь  был  успешно деавторизован

    USER_MESSAGE_SENT(28), // Служебное  сообщение, пользователь печатает сообщение
    USER_STOPPED_TYPING(29), // Служебное  сообщение, пользователь перестал печатать

    INITIAL_MESSAGE(30), //сообщение, которое регулируется полем showInitialMessage


    DEFAULT(-1),
    INFO_MESSAGE(-2);

    companion object {
        fun getMessageTypeByValueType(valueType: Int): MessageType {
            return when(valueType) {
                0 -> INITIAL
                1 -> MESSAGE
                2 -> SENT_CONFIRMATION
                4 -> READING_CONFIRMATION
                5 -> FAILED_CONFIRMATION
                8 -> DELETED
                9 -> PHONE_NUMBER
                10 -> LOCATION
                11 -> RECEIVED_BY_MEDIATOR
                12 -> RECEIVED_BY_OPERATOR
                13 -> OPERATOR_IS_TYPING
                14 -> OPERATOR_STOPPED_TYPING
                15 -> UPDATE_DIALOG_SCORE
                16 -> FINISH_DIALOG
                17 -> CLOSE_DIALOG_INTENTION
                18 -> CONNECTED_OPERATOR
                19 -> UPDATE_NEGATIVE_REASON
                20 -> AUTO_GREETING
                21 -> UPDATE_DIALOG_NEGATIVE_REASON
                22 -> UPDATE_USER_DATA
                23 -> CLIENT_HOLD
                24 -> UPDATE_DIALOG_USEFULNESS
                25 -> USER_WAS_MERGED
                26 -> USER_WAS_AUTHORIZED
                27 -> USER_WAS_DEAUTHORIZED
                28 -> USER_MESSAGE_SENT
                29 -> USER_STOPPED_TYPING
                30 -> INITIAL_MESSAGE
                -2 -> INFO_MESSAGE
                else -> DEFAULT
            }
        }
    }

}