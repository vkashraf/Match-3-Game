package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.BuildingDao
import com.example.data.local.dao.BuildingPlotDao
import com.example.data.local.dao.DailyRewardDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.IslandZoneDao
import com.example.data.local.dao.LevelProgressDao
import com.example.data.local.dao.MissionDao
import com.example.data.local.dao.PlayerDao
import com.example.data.local.dao.PlayerStatsDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.entity.AchievementProgressEntity
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.BuildingPlotEntity
import com.example.data.local.entity.DailyRewardStateEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.IslandZoneEntity
import com.example.data.local.entity.LevelProgressEntity
import com.example.data.local.entity.MissionProgressEntity
import com.example.data.local.entity.PlayerEntity
import com.example.data.local.entity.PlayerStatsEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.dao.WorldProgressDao
import com.example.data.local.entity.WorldProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.local.dao.DecorationDao
import com.example.data.local.entity.DecorationEntity
import com.example.data.local.dao.PendingRewardDao
import com.example.data.local.entity.PendingRewardEntity

@Database(
    entities = [
        PlayerEntity::class,
        LevelProgressEntity::class,
        BuildingEntity::class,
        BuildingPlotEntity::class,
        IslandZoneEntity::class,
        InventoryItemEntity::class,
        SettingsEntity::class,
        DailyRewardStateEntity::class,
        MissionProgressEntity::class,
        AchievementProgressEntity::class,
        PlayerStatsEntity::class,
        WorldProgressEntity::class,
        PendingRewardEntity::class,
        DecorationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun worldProgressDao(): WorldProgressDao
    abstract fun buildingDao(): BuildingDao
    abstract fun buildingPlotDao(): BuildingPlotDao
    abstract fun islandZoneDao(): IslandZoneDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun missionDao(): MissionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun pendingRewardDao(): PendingRewardDao
    abstract fun decorationDao(): DecorationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "isle_match_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Player
            if (db.playerDao().getPlayer() == null) {
                db.playerDao().insertPlayer(
                    PlayerEntity(
                        playerId = 1,
                        playerName = "Player",
                        playerLevel = 1,
                        xp = 0,
                        coins = 1000,
                        gems = 50,
                        energy = 5,
                        maxEnergy = 5,
                        totalStars = 0,
                        currentLevel = 1
                    )
                )
            }

            // Initial Levels (1-200)
            val currentCount = db.levelProgressDao().getLevelCount()
            if (currentCount < 200) {
                val levels = mutableListOf<LevelProgressEntity>()
                for (i in (currentCount + 1)..200) {
                    levels.add(
                        LevelProgressEntity(
                            levelId = i,
                            isUnlocked = (i == 1),
                            isCompleted = false,
                            stars = 0,
                            bestScore = 0,
                            bestMoves = 0
                        )
                    )
                }
                db.levelProgressDao().insertLevels(levels)
            }

            // Initial World Progress (1-10)
            if (db.worldProgressDao().getWorldCount() == 0) {
                val worlds = mutableListOf<WorldProgressEntity>()
                for (i in 1..10) {
                    worlds.add(
                        WorldProgressEntity(
                            worldId = i,
                            isUnlocked = (i == 1),
                            isCompleted = false,
                            rewardClaimed = false,
                            unlockedAt = if (i == 1) System.currentTimeMillis() else 0
                        )
                    )
                }
                db.worldProgressDao().insertWorlds(worlds)
            }
            if (db.islandZoneDao().getZoneCount() == 0) {
                val zones = listOf(
                    IslandZoneEntity(1, "Starter Village", isUnlocked = true, requiredPlayerLevel = 1, requiredStars = 0, requiredCompletedLevel = 1, unlockCostCoins = 0),
                    IslandZoneEntity(2, "Farm Area", isUnlocked = true, requiredPlayerLevel = 1, requiredStars = 0, requiredCompletedLevel = 1, unlockCostCoins = 0),
                    IslandZoneEntity(3, "Mining Area", isUnlocked = false, requiredPlayerLevel = 3, requiredStars = 10, requiredCompletedLevel = 5, unlockCostCoins = 300),
                    IslandZoneEntity(4, "Workshop Area", isUnlocked = false, requiredPlayerLevel = 5, requiredStars = 20, requiredCompletedLevel = 10, unlockCostCoins = 600),
                    IslandZoneEntity(5, "Harbor Area", isUnlocked = false, requiredPlayerLevel = 8, requiredStars = 35, requiredCompletedLevel = 20, unlockCostCoins = 1200),
                    IslandZoneEntity(6, "Advanced Area", isUnlocked = false, requiredPlayerLevel = 12, requiredStars = 50, requiredCompletedLevel = 30, unlockCostCoins = 2500)
                )
                db.islandZoneDao().insertZones(zones)
            }

            // Initial Plots
            if (db.buildingPlotDao().getPlotCount() == 0) {
                val plots = listOf(
                    BuildingPlotEntity("plot_house", zoneId = 1, x = 380f, y = 740f, isUnlocked = true, buildingId = "HOUSE"),
                    BuildingPlotEntity("plot_farm", zoneId = 2, x = 180f, y = 620f, isUnlocked = true, buildingId = "FARM"),
                    BuildingPlotEntity("plot_1_empty", zoneId = 1, x = 300f, y = 820f, isUnlocked = true, buildingId = null),
                    BuildingPlotEntity("plot_3_mine", zoneId = 3, x = 550f, y = 880f, isUnlocked = false, buildingId = null, requiredPlayerLevel = 3, requiredStars = 10, unlockCostCoins = 300),
                    BuildingPlotEntity("plot_4_workshop", zoneId = 4, x = 460f, y = 500f, isUnlocked = false, buildingId = null, requiredPlayerLevel = 5, requiredStars = 20, unlockCostCoins = 600),
                    BuildingPlotEntity("plot_4_bakery", zoneId = 4, x = 580f, y = 420f, isUnlocked = false, buildingId = null, requiredPlayerLevel = 6, requiredStars = 25, unlockCostCoins = 800),
                    BuildingPlotEntity("plot_5_harbor", zoneId = 5, x = 220f, y = 360f, isUnlocked = false, buildingId = null, requiredPlayerLevel = 8, requiredStars = 35, unlockCostCoins = 1200),
                    BuildingPlotEntity("plot_6_magic", zoneId = 6, x = 700f, y = 680f, isUnlocked = false, buildingId = null, requiredPlayerLevel = 12, requiredStars = 50, unlockCostCoins = 2500)
                )
                db.buildingPlotDao().insertPlots(plots)
            }

            // Initial Buildings
            if (db.buildingDao().getBuildingCount() == 0) {
                val buildings = listOf(
                    BuildingEntity("HOUSE", "HOUSE", plotId = "plot_house", level = 1, isBuilt = true, isConstructing = false, productionPerHour = 10),
                    BuildingEntity("FARM", "FARM", plotId = "plot_farm", level = 1, isBuilt = true, isConstructing = false, productionPerHour = 20)
                )
                db.buildingDao().insertBuildings(buildings)
            }

            // Initial Settings
            if (db.settingsDao().getSettings() == null) {
                db.settingsDao().insertSettings(SettingsEntity())
            }

            // Initial Inventory Boosters
            if (db.inventoryDao().getItem("HAMMER") == null) {
                db.inventoryDao().insertItem(InventoryItemEntity("HAMMER", "BOOSTER", 3))
                db.inventoryDao().insertItem(InventoryItemEntity("SWAP", "BOOSTER", 2))
                db.inventoryDao().insertItem(InventoryItemEntity("SHUFFLE", "BOOSTER", 1))
                db.inventoryDao().insertItem(InventoryItemEntity("EXTRA_MOVES", "BOOSTER", 1))
            }
        }
    }
}
