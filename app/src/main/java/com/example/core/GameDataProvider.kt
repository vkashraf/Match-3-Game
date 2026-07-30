package com.example.core

import android.content.Context
import com.example.core.event.GameEventBus
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlayerEntity
import com.example.data.repository.AchievementRepository
import com.example.data.repository.BuildingRepository
import com.example.data.repository.DailyRewardRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.IslandRepository
import com.example.data.repository.WorldRepository
import com.example.data.repository.LevelRepository
import com.example.data.repository.MissionRepository
import com.example.data.repository.PlayerRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.StatsRepository
import com.example.game.energy.EnergyManager
import com.example.game.shop.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object GameDataProvider {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    lateinit var playerRepository: PlayerRepository
        private set
    lateinit var levelRepository: LevelRepository
        private set
    lateinit var worldRepository: WorldRepository
        private set
    lateinit var buildingRepository: BuildingRepository
        private set
    lateinit var islandRepository: IslandRepository
        private set
    lateinit var inventoryRepository: InventoryRepository
        private set
    lateinit var shopRepository: ShopRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var dailyRewardRepository: DailyRewardRepository
        private set
    lateinit var missionRepository: MissionRepository
        private set
    lateinit var achievementRepository: AchievementRepository
        private set
    lateinit var statsRepository: StatsRepository
        private set
    lateinit var pendingRewardRepository: com.example.data.repository.PendingRewardRepository
        private set
    lateinit var decorationRepository: com.example.data.repository.DecorationRepository
        private set

    private val _cachedPlayer = MutableStateFlow<PlayerEntity?>(null)
    val cachedPlayer: StateFlow<PlayerEntity?> = _cachedPlayer.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        val db = AppDatabase.getInstance(context)

        playerRepository = PlayerRepository(db.playerDao())
        levelRepository = LevelRepository(db.levelProgressDao())
        worldRepository = WorldRepository(db.worldProgressDao())
        buildingRepository = BuildingRepository(db.buildingDao())
        islandRepository = IslandRepository(db.buildingPlotDao(), db.islandZoneDao())
        inventoryRepository = InventoryRepository(db.inventoryDao())
        shopRepository = ShopRepository(playerRepository, inventoryRepository)
        settingsRepository = SettingsRepository(db.settingsDao())
        dailyRewardRepository = DailyRewardRepository(db.dailyRewardDao())
        missionRepository = MissionRepository(db.missionDao())
        achievementRepository = AchievementRepository(db.achievementDao())
        statsRepository = StatsRepository(db.playerStatsDao())
        pendingRewardRepository = com.example.data.repository.PendingRewardRepository(db.pendingRewardDao())
        decorationRepository = com.example.data.repository.DecorationRepository(db.decorationDao())

        isInitialized = true

        // Observe player changes and keep cached copy updated for LibGDX render loops
        scope.launch {
            playerRepository.playerFlow.collect { player ->
                _cachedPlayer.value = player
            }
        }

        // Ensure player is initialized and check energy offline regeneration
        scope.launch {
            val p = EnergyManager.checkAndApplyRegeneration(playerRepository)
            _cachedPlayer.value = p
        }

        // Listen to GameEventBus and process events across repositories
        scope.launch {
            GameEventBus.events.collect { event ->
                missionRepository.onGameEvent(event)
                achievementRepository.onGameEvent(event)
                statsRepository.onGameEvent(event)

                // Process XP rewards
                when (event.type) {
                    com.example.core.event.GameEventType.LEVEL_COMPLETED -> {
                        var xpGain = com.example.game.level.XPRewardConfig.XP_LEVEL_COMPLETED
                        if (event.amount >= 3) { // 3 stars
                            xpGain += com.example.game.level.XPRewardConfig.XP_THREE_STARS_BONUS
                        }
                        com.example.game.level.PlayerLevelManager.addXpAndCheckLevelUp(xpGain)
                    }
                    com.example.core.event.GameEventType.BUILDING_COMPLETED -> {
                        com.example.game.level.PlayerLevelManager.addXpAndCheckLevelUp(com.example.game.level.XPRewardConfig.XP_BUILDING_COMPLETED)
                    }
                    com.example.core.event.GameEventType.BUILDING_UPGRADED -> {
                        com.example.game.level.PlayerLevelManager.addXpAndCheckLevelUp(com.example.game.level.XPRewardConfig.XP_BUILDING_UPGRADED)
                    }
                    com.example.core.event.GameEventType.LAND_UNLOCKED -> {
                        com.example.game.level.PlayerLevelManager.addXpAndCheckLevelUp(com.example.game.level.XPRewardConfig.XP_LAND_EXPANSION)
                    }
                    else -> {}
                }
            }
        }

        // Pre-load daily missions, achievements, and starter boosters
        scope.launch {
            missionRepository.ensureDailyMissionsLoaded()
            achievementRepository.ensureAchievementsLoaded()
            dailyRewardRepository.getOrCreateState()
            statsRepository.getOrCreateStats()

            // Initialize starter boosters for new players
            if (!inventoryRepository.hasItem("HAMMER", 1)) inventoryRepository.addItem("HAMMER", 3)
            if (!inventoryRepository.hasItem("SWAP", 1)) inventoryRepository.addItem("SWAP", 3)
            if (!inventoryRepository.hasItem("SHUFFLE", 1)) inventoryRepository.addItem("SHUFFLE", 3)
            if (!inventoryRepository.hasItem("EXTRA_MOVES", 1)) inventoryRepository.addItem("EXTRA_MOVES", 3)
            if (!inventoryRepository.hasItem("ROW_CLEAR", 1)) inventoryRepository.addItem("ROW_CLEAR", 2)
            if (!inventoryRepository.hasItem("COLOR_REMOVE", 1)) inventoryRepository.addItem("COLOR_REMOVE", 2)
            if (!inventoryRepository.hasItem("BOMB_START", 1)) inventoryRepository.addItem("BOMB_START", 2)
            if (!inventoryRepository.hasItem("ROCKET_START", 1)) inventoryRepository.addItem("ROCKET_START", 2)
            if (!inventoryRepository.hasItem("RAINBOW_START", 1)) inventoryRepository.addItem("RAINBOW_START", 2)
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
