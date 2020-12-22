package com.crafttalk.sampleChat

import android.content.Context
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.utils.HashUtils
import com.crafttalk.sampleChat.DataVisitor.birthday
import com.crafttalk.sampleChat.DataVisitor.contract
import com.crafttalk.sampleChat.DataVisitor.email
import com.crafttalk.sampleChat.DataVisitor.firstName
import com.crafttalk.sampleChat.DataVisitor.lastName
import com.crafttalk.sampleChat.DataVisitor.phone
import com.crafttalk.sampleChat.DataVisitor.source
import com.crafttalk.sampleChat.DataVisitor.uuid

fun getVisitor(context: Context): Visitor = Visitor(
    uuid,
    firstName,
    lastName,
    email,
    phone,
    contract,
    birthday,
    HashUtils.getHash("SHA-256", "${getSalt(context)}${HashUtils.getHash("SHA-256", "${getSalt(context)}${source}")}")
)

object DataVisitor {
    const val uuid = "Karl_31"
    const val firstName = "Karl_31"
    const val lastName = "Testovich_Ivanovich"
    const val email = "email"
    const val phone = "000000000"
    const val contract = "contract_test"
    const val birthday = "00.00.00"
    const val source = "${uuid}${firstName}${lastName}${contract}${phone}${email}${birthday}"
}

fun getSalt(context: Context): String = context.getString(R.string.salt)