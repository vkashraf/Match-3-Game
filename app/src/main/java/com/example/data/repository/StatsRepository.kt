package com.example.data.repository

import com.example.core.event.GameEvent
import com.example.core.event.GameEventType
import com.example.data.local.dao.PlayerStatsDao
import com.example.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StatsRepository(private val playerStatsDao: PlayerStatsDao) {

    val statsFlow: Flow<PlayerStatsEntity?> = playerStatsDao.observePlayerStats()

    suspend fun getOrCreateStats(): PlayerStatsEntity = withContext(Dispatchers.IO) {
        val existing = playerStatsDao.getPlayerStats()
        if (existing != null) {
            existing
        } else {
            val defaultStats = PlayerStatsEntity(id = 1)
            playerStatsDao.insertOrUpdateStats(defaultStats)
            defaultStats
        }
    }

    suspend fun onGameEvent(event: GameEvent) = withContext(Dispatchers.IO) {
        val stats = getOrCreateStats()
        val updated = when (event.type) {
            GameEventType.LEVEL_STARTED -> stats.copy(totalLevelsPlayed = stats.totalLevelsPlayed + event.amount)
            GameEventType.LEVEL_COMPLETED -> stats.copy(totalLevelsWon = stats.totalLevelsWon + event.amount)
            GameEventType.LEVEL_FAILED -> stats.copy(totalLevelsLost = stats.totalLevelsLost + event.amount)
            GameEventType.TILES_CLEARED -> stats.copy(totalTilesCleared = stats.totalTilesCleared + event.amount)
            GameEventType.SPECIAL_ACTIVATED -> stats.copy(totalSpecialTilesActivated = stats.totalSpecialTilesActivated + event.amount)
            GameEventType.OBSTACLE_DESTROYED -> stats.copy(totalObstaclesDestroyed = stats.totalObstaclesDestroyed + event.amount)
            GameEventType.BOOSTER_USED -> stats.copy(totalBoostersUsed = stats.totalBoostersUsed + event.amount)
            GameEventType.COINS_EARNED -> stats.copy(totalCoinsEarned = stats.totalCoinsEarned + event.amount)
            GameEventType.GEMS_EARNED -> stats.copy(totalGemsEarned = stats.totalGemsEarned + event.amount)
            GameEventType.BUILDING_BUILT -> stats.copy(totalBuildingsBuilt = stats.totalBuildingsBuilt + event.amount)
            GameEventType.BUILDING_UPGRADED -> stats.copy(totalBuildingsUpgraded = stats.totalBuildingsUpgraded + event.amount)
            GameEventType.STAR_EARNED -> stats.copy(totalStarsEarned = stats.totalStarsEarned + event.amount)
            else -> stats
        }

        if (updated != stats) {
            playerStatsDao.insertOrUpdateStats(updated)
        }
    }

    suspend fun updateLongestStreak(streak: Int) = withContext(Dispatchers.IO) {
        val stats = getOrCreateStats()
        if (streak > stats.longestDailyStreak) {
            playerStatsDao.insertOrUpdateStats(stats.copy(longestDailyStreak = streak))
        }
    }
}
