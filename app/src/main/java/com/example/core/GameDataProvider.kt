package com.example.core

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlayerEntity
import com.example.data.repository.BuildingRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.IslandRepository
import com.example.data.repository.LevelRepository
import com.example.data.repository.PlayerRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object GameDataProvider {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    lateinit var playerRepository: PlayerRepository
        private set
    lateinit var levelRepository: LevelRepository
        private set
    lateinit var buildingRepository: BuildingRepository
        private set
    lateinit var islandRepository: IslandRepository
        private set
    lateinit var inventoryRepository: InventoryRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    private val _cachedPlayer = MutableStateFlow<PlayerEntity?>(null)
    val cachedPlayer: StateFlow<PlayerEntity?> = _cachedPlayer.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        val db = AppDatabase.getInstance(context)

        playerRepository = PlayerRepository(db.playerDao())
        levelRepository = LevelRepository(db.levelProgressDao())
        buildingRepository = BuildingRepository(db.buildingDao())
        islandRepository = IslandRepository(db.buildingPlotDao(), db.islandZoneDao())
        inventoryRepository = InventoryRepository(db.inventoryDao())
        settingsRepository = SettingsRepository(db.settingsDao())

        isInitialized = true

        // Observe player changes and keep cached copy updated for LibGDX render loops
        scope.launch {
            playerRepository.playerFlow.collect { player ->
                _cachedPlayer.value = player
            }
        }

        // Ensure player is initialized
        scope.launch {
            val p = playerRepository.getPlayer()
            _cachedPlayer.value = p
        }
    }

    fun addCoins(amount: Long) {
        scope.launch { playerRepository.addCoins(amount) }
    }

    fun spendCoins(amount: Long) {
        scope.launch { playerRepository.spendCoins(amount) }
    }

    fun addGems(amount: Int) {
        scope.launch { playerRepository.addGems(amount) }
    }

    fun spendGems(amount: Int) {
        scope.launch { playerRepository.spendGems(amount) }
    }

    fun addEnergy(amount: Int) {
        scope.launch { playerRepository.addEnergy(amount) }
    }

    fun spendEnergy(amount: Int) {
        scope.launch { playerRepository.spendEnergy(amount) }
    }

    fun addXp(amount: Long) {
        scope.launch { playerRepository.addXp(amount) }
    }

    fun addStars(amount: Int) {
        scope.launch { playerRepository.addStars(amount) }
    }
}
