package com.crafttalk.chat.data.model

import com.crafttalk.chat.utils.HashUtils
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
    val birthday: String?//, // дата рождения клиента
//    val fb: String?,
//    val vk: String?,
//    val hash: String?, // хэш клиента
//    val subscription: Any?, // содержит данные о подписке клиента на push-уведомления
//    val url:String?, // содержитадресстраницы,скоторойполученыданныеоподпискеклиента на push-уведомления
//    val unread_msg: Int? // количество непрочитанных клиентом сообщений
    ) {

    private val salt = "C~kW]cq76(a?m[UbJ)drw+Wh6>W[3Wsj"
    private val source = uuid + firstName + lastName + contract + phone + email + birthday
    private val hash = HashUtils.hashString("SHA-256", (salt + HashUtils.hashString("SHA-256", salt + source)))

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
        visitorJson.put("contract", contract)
        visitorJson.put("birthday", birthday)
        visitorJson.put("hash", hash)
        return visitorJson
    }

}