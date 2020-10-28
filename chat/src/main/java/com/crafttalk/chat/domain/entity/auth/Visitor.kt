package com.crafttalk.chat.domain.entity.auth

import com.crafttalk.chat.data.local.pref.Uuid
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class Visitor (
    val uuid: String,
    @SerializedName(value = "first_name")
    val firstName: String,
    @SerializedName(value = "last_name")
    val lastName: String,
    val email: String?,
    val phone: String?,
    val contract: String?, // номер контракта клиента
    val birthday: String?, // дата рождения клиента
    val hash: String? = null
//    val fb: String?,
//    val vk: String?,
//    val subscription: Any?, // содержит данные о подписке клиента на push-уведомления
//    val url:String?, // содержитадресстраницы,скоторойполученыданныеоподпискеклиента на push-уведомления
//    val unread_msg: Int? // количество непрочитанных клиентом сообщений
) {

    override fun toString(): String {
        return "${uuid}, ${firstName}, ${lastName}, ${email}, ${phone}, ${contract}, ${birthday}"
    }

    fun getJsonObject(): JSONObject {
        val visitorJson = JSONObject()
        visitorJson.put("uuid", uuid)
        visitorJson.put("first_name", firstName)
        visitorJson.put("last_name", lastName)
        visitorJson.put("email", email)
        visitorJson.put("phone", phone)
        visitorJson.put("contract", contract ?: DEFAULT_CONTRACT)
        visitorJson.put("birthday", birthday)
        visitorJson.put("hash", hash)
        return visitorJson
    }

    companion object {

        private const val DEFAULT_FIRST_NAME = "user"
        private const val DEFAULT_LAST_NAME = "userovich"
        private const val DEFAULT_PHONE = "0"
        private const val DEFAULT_MAIL = "test@gmail.com"
        private const val DEFAULT_CONTRACT = "test_contract"
        private const val DEFAULT_BIRTHDAY = "28.05.1975"

        // это же массив причем тут null; мб может упасть...
        fun map(args: Array<out String>): Visitor {
            var firstName: String
            var lastName: String
            var phone: String
            try {
                firstName = args[0]
                lastName = args[1]
                phone = args[2]
            } catch (ex: Exception) {
                firstName = DEFAULT_FIRST_NAME
                lastName = DEFAULT_LAST_NAME
                phone = DEFAULT_PHONE
            }

            return Visitor(
                Uuid.generateUUID(
                    true,
                    firstName,
                    lastName
                ),
                firstName,
                lastName,
                DEFAULT_MAIL,
                phone,
                DEFAULT_CONTRACT,
                DEFAULT_BIRTHDAY
            )
        }

    }

}