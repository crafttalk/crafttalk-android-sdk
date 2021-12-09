package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.rest.ConfigurationApi
import com.crafttalk.chat.data.helper.network.toData
import com.crafttalk.chat.domain.repository.IConfigurationRepository
import javax.inject.Inject

class ConfigurationRepository
@Inject constructor(
    private val configurationApi: ConfigurationApi
) : IConfigurationRepository {
    override fun getConfiguration() = configurationApi.getConfiguration().toData()
}