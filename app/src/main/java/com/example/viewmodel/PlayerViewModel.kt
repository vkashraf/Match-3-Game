package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.LevelProgressEntity
import com.example.data.local.entity.PlayerEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.repository.BuildingRepository
import com.example.data.repository.LevelRepository
import com.example.data.repository.PlayerRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerRepository: PlayerRepository,
    private val levelRepository: LevelRepository,
    private val buildingRepository: BuildingRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerEntity?> = playerRepository.playerFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val levelsState: StateFlow<List<LevelProgressEntity>> = levelRepository.allLevelsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val buildingsState: StateFlow<List<BuildingEntity>> = buildingRepository.allBuildingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settingsState: StateFlow<SettingsEntity?> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun addCoins(amount: Long) {
        viewModelScope.launch {
            playerRepository.addCoins(amount)
        }
    }

    fun spendCoins(amount: Long) {
        viewModelScope.launch {
            playerRepository.spendCoins(amount)
        }
    }

    fun addGems(amount: Int) {
        viewModelScope.launch {
            playerRepository.addGems(amount)
        }
    }

    fun spendGems(amount: Int) {
        viewModelScope.launch {
            playerRepository.spendGems(amount)
        }
    }

    fun addEnergy(amount: Int) {
        viewModelScope.launch {
            playerRepository.addEnergy(amount)
        }
    }

    fun spendEnergy(amount: Int) {
        viewModelScope.launch {
            playerRepository.spendEnergy(amount)
        }
    }

    fun addXp(amount: Long) {
        viewModelScope.launch {
            playerRepository.addXp(amount)
        }
    }

    fun addStars(amount: Int) {
        viewModelScope.launch {
            playerRepository.addStars(amount)
        }
    }
}
