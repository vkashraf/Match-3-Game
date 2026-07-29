package com.example.data.repository

import com.example.data.local.dao.SettingsDao
import com.example.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    val settingsFlow: Flow<SettingsEntity?> = settingsDao.observeSettings()

    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettings() ?: SettingsEntity().also { settingsDao.insertSettings(it) }
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.updateSettings(settings)
    }
}
