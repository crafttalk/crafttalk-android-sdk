package com.crafttalk.chat.di

import javax.inject.Qualifier

@Qualifier
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Upload

@Qualifier
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Notification