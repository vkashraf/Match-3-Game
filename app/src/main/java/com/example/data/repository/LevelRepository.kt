package com.example.data.repository

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.data.local.dao.LevelProgressDao
import com.example.data.local.entity.LevelProgressEntity
import com.example.game.world.WorldConfigRepository
import kotlinx.coroutines.flow.Flow

class LevelRepository(private val levelProgressDao: LevelProgressDao) {

    val allLevelsFlow: Flow<List<LevelProgressEntity>> = levelProgressDao.getAllLevels()

    fun observeLevel(levelId: Int): Flow<LevelProgressEntity?> = levelProgressDao.observeLevel(levelId)

    suspend fun getLevel(levelId: Int): LevelProgressEntity? = levelProgressDao.getLevel(levelId)

    suspend fun unlockLevel(levelId: Int) {
        levelProgressDao.unlockLevel(levelId)
        val worldId = ((levelId - 1) / WorldConfigRepository.LEVELS_PER_WORLD) + 1
        GameEventBus.postEvent(
            GameEvent(
                type = GameEventType.LEVEL_UNLOCKED,
                levelId = levelId,
                worldId = worldId
            )
        )
    }

    suspend fun completeLevel(levelId: Int, stars: Int, score: Int, movesLeft: Int) {
        val existing = getLevel(levelId) ?: return
        val newStars = maxOf(existing.stars, stars)
        val newScore = maxOf(existing.bestScore, score)
        val newMoves = maxOf(existing.bestMoves, movesLeft)
        val newPlayed = existing.timesPlayed + 1
        val newWon = existing.timesWon + 1
        
        val updated = existing.copy(
            isCompleted = true,
            stars = newStars,
            bestScore = newScore,
            bestMoves = newMoves,
            completedAt = System.currentTimeMillis(),
            timesPlayed = newPlayed,
            timesWon = newWon,
            lastPlayedAt = System.currentTimeMillis()
        )
        levelProgressDao.updateLevel(updated)

        // Automatically unlock next level
        val nextLevelId = levelId + 1
        if (nextLevelId <= WorldConfigRepository.TOTAL_LEVELS) {
            unlockLevel(nextLevelId)
        }

        if (levelId == WorldConfigRepository.TOTAL_LEVELS) {
            GameEventBus.postEvent(
                GameEvent(
                    type = GameEventType.CHAPTER_COMPLETED
                )
            )
        }

        // Check world completion
        val worldId = ((levelId - 1) / WorldConfigRepository.LEVELS_PER_WORLD) + 1
        val worldLevels = mutableListOf<LevelProgressEntity>()
        val startLvl = (worldId - 1) * WorldConfigRepository.LEVELS_PER_WORLD + 1
        val endLvl = worldId * WorldConfigRepository.LEVELS_PER_WORLD
        for (lId in startLvl..endLvl) {
            val lvl = getLevel(lId)
            if (lvl != null) worldLevels.add(lvl)
        }
        val totalPlayerStars = GameDataProvider.cachedPlayer.value?.totalStars ?: 0
        GameDataProvider.worldRepository.checkAndUpdateWorldProgress(worldId, worldLevels, totalPlayerStars)
    }

    suspend fun recordLoss(levelId: Int) {
        val existing = getLevel(levelId) ?: return
        val updated = existing.copy(
            timesPlayed = existing.timesPlayed + 1,
            timesLost = existing.timesLost + 1,
            lastPlayedAt = System.currentTimeMillis()
        )
        levelProgressDao.updateLevel(updated)
    }
}
