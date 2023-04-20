package com.crafttalk.sampleChat.chat_with_counter

import android.content.Context
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.utils.HashUtils
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.birthday
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.contract
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.email
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.firstName
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.lastName
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.phone
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.source
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.token
import com.crafttalk.sampleChat.chat_with_counter.DataVisitor.uuid

fun getVisitor(context: Context): Visitor = Visitor(
    uuid,
    token,
    firstName,
    lastName,
    email,
    phone,
    contract,
    birthday,
    HashUtils.getHash("SHA-256", "${getSalt(context)}${HashUtils.getHash("SHA-256", "${getSalt(context)}${source}")}")
)

object DataVisitor {
    const val uuid = "test_uuid"
    const val token = "test_token"
    const val firstName = "Karl"
    const val lastName = "Testovich"
    const val email = "email"
    const val phone = "000000000"
    const val contract = "contract_test"
    const val birthday = "00.00.00"
    const val source = "$uuid$firstName$lastName$contract$phone$email$birthday"
}

fun getSalt(context: Context): String = context.getString(R.string.salt)