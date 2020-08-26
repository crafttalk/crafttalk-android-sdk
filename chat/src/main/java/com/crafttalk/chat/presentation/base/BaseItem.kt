package com.crafttalk.chat.presentation.base

abstract class BaseItem {
    abstract fun getLayout() : Int
    abstract fun <T : BaseItem> isSame(item: T) : Boolean
}