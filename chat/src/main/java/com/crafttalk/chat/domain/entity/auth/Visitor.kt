package com.crafttalk.chat.domain.entity.auth

import com.crafttalk.chat.utils.getOrNull
import com.crafttalk.chat.utils.getStringOrNull
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

class Visitor (
    val uuid: String,
    val token: String,
    @SerializedName(value = "first_name")
    val firstName: String,
    @SerializedName(value = "last_name")
    val lastName: String,
    val email: String?,
    val phone: String?,
    val contract: String?, // номер контракта клиента
    val birthday: String?, // дата рождения клиента
    val hash: String? = null
) : Serializable {

    private val addedFields = HashMap<String, Any>()

    override fun toString(): String {
        return "${uuid}, ${token}, ${firstName}, ${lastName}, ${email}, ${phone}, ${contract}, ${birthday}"
    }

    fun addNewField(fieldName: String, fieldValue: Any) {
        addedFields[fieldName] = fieldValue
    }

    fun removeAddedField(firstName: String) {
        addedFields.remove(firstName)
    }

    fun getJsonObject(): JSONObject {
        val visitorJson = JSONObject()
        visitorJson.put("uuid", uuid)
        visitorJson.put("token", token)
        visitorJson.put("first_name", firstName)
        visitorJson.put("last_name", lastName)
        visitorJson.put("email", email)
        visitorJson.put("phone", phone)
        visitorJson.put("contract", contract)
        visitorJson.put("birthday", birthday)
        visitorJson.put("hash", hash)
        if (addedFields != null) {
            for ((key, value) in addedFields) {
                visitorJson.put(key, value)
            }
        }
        return visitorJson
    }

    companion object {

        private const val DEFAULT_FIRST_NAME = "user"
        private const val DEFAULT_LAST_NAME = "userovich"
        private const val DEFAULT_PHONE = "0"
        private const val DEFAULT_MAIL = "test@gmail.com"
        private const val DEFAULT_CONTRACT = "test_contract"
        private const val DEFAULT_BIRTHDAY = "28.05.1975"

        fun map(args: Array<out String>, fields: HashMap<String, Any>?): Visitor {
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

            val uuid = UUID.randomUUID().toString()

            return Visitor(
                uuid,
                "${uuid}default",
                firstName,
                lastName,
                DEFAULT_MAIL,
                phone,
                DEFAULT_CONTRACT,
                DEFAULT_BIRTHDAY
            ).apply {
                addedFields.clear()
                fields?.forEach {
                    addNewField(it.key, it.value)
                }
            }
        }

        fun getVisitorFromJson(json: String?): Visitor? {
            json ?: return null
            val jsonObject = JSONObject(json)
            val visitor = Visitor(
                uuid = jsonObject.getStringOrNull("uuid").orEmpty(),
                token = jsonObject.getStringOrNull("token").orEmpty(),
                firstName = jsonObject.getStringOrNull("first_name").orEmpty(),
                lastName = jsonObject.getStringOrNull("last_name").orEmpty(),
                email = jsonObject.getStringOrNull("email"),
                phone = jsonObject.getStringOrNull("phone"),
                contract = jsonObject.getStringOrNull("contract"),
                birthday = jsonObject.getStringOrNull("birthday"),
                hash = jsonObject.getStringOrNull("hash")
            )
            visitor.addedFields.clear()
            jsonObject.keys()
                .asSequence()
                .filter { it !in listOf(
                    "uuid",
                    "token",
                    "first_name",
                    "last_name",
                    "email",
                    "phone",
                    "contract",
                    "birthday",
                    "hash"
                ) }
                .forEach { key ->
                    jsonObject.getOrNull(key.toString())?.let { value ->
                        visitor.addNewField(key.toString(), value)
                    }
                }
            return visitor
        }
    }

}