package com.example.data.repository

import com.example.data.local.dao.LevelProgressDao
import com.example.data.local.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

class LevelRepository(private val levelProgressDao: LevelProgressDao) {

    val allLevelsFlow: Flow<List<LevelProgressEntity>> = levelProgressDao.getAllLevels()

    fun observeLevel(levelId: Int): Flow<LevelProgressEntity?> = levelProgressDao.observeLevel(levelId)

    suspend fun getLevel(levelId: Int): LevelProgressEntity? = levelProgressDao.getLevel(levelId)

    suspend fun unlockLevel(levelId: Int) {
        levelProgressDao.unlockLevel(levelId)
    }

    suspend fun completeLevel(levelId: Int, stars: Int, score: Int, movesLeft: Int) {
        val existing = getLevel(levelId) ?: return
        val newStars = maxOf(existing.stars, stars)
        val newScore = maxOf(existing.bestScore, score)
        val newMoves = maxOf(existing.bestMoves, movesLeft)
        
        val updated = existing.copy(
            isCompleted = true,
            stars = newStars,
            bestScore = newScore,
            bestMoves = newMoves,
            completedAt = System.currentTimeMillis()
        )
        levelProgressDao.updateLevel(updated)

        // Automatically unlock next level
        levelProgressDao.unlockLevel(levelId + 1)
    }
}
