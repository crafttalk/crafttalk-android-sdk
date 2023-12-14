package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.google.gson.annotations.SerializedName

data class NetworkAction (
    @SerializedName(value = "action_id")
    var actionId: String,
    @SerializedName (value = "action_text")
    val actionText: String
) {

    companion object {

        fun map(actionEntity: ActionEntity) = NetworkAction(
            actionId = actionEntity.actionId,
            actionText = actionEntity.actionText
        )
    }
}