package com.example.data.repository

import com.example.model.PlayerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository {
    private val _playerData = MutableStateFlow(PlayerData())
    val playerData: Flow<PlayerData> = _playerData.asStateFlow()

    fun updatePlayerData(newData: PlayerData) {
        _playerData.value = newData
    }
}
