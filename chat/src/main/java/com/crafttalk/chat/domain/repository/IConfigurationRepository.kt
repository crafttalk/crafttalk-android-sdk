package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.configuration.NetworkResultConfiguration

interface IConfigurationRepository {
    fun getConfiguration(): NetworkResultConfiguration?
}