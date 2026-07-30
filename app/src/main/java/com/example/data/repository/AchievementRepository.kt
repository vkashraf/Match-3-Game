package com.example.data.repository

import com.example.core.GameTimeProvider
import com.example.core.event.GameEvent
import com.example.data.local.dao.AchievementDao
import com.example.data.local.entity.AchievementProgressEntity
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AchievementRepository(private val achievementDao: AchievementDao) {

    val allAchievementsFlow: Flow<List<AchievementProgressEntity>> = achievementDao.observeAllAchievements()

    suspend fun ensureAchievementsLoaded(): List<AchievementProgressEntity> = withContext(Dispatchers.IO) {
        val existing = achievementDao.getAllAchievements()
        val existingMap = existing.associateBy { it.achievementId }

        val toInsert = mutableListOf<AchievementProgressEntity>()
        val resultList = mutableListOf<AchievementProgressEntity>()

        for (config in AchievementData.ACHIEVEMENTS) {
            val entity = existingMap[config.achievementId]
            if (entity == null) {
                val newEntity = AchievementProgressEntity(
                    achievementId = config.achievementId,
                    currentProgress = 0,
                    target = config.target,
                    isCompleted = false,
                    isRewardClaimed = false,
                    updatedAt = GameTimeProvider.getCurrentTimeMillis()
                )
                toInsert.add(newEntity)
                resultList.add(newEntity)
            } else {
                resultList.add(entity)
            }
        }

        if (toInsert.isNotEmpty()) {
            achievementDao.insertOrUpdateAchievements(toInsert)
        }
        resultList
    }

    suspend fun onGameEvent(event: GameEvent): List<AchievementConfig> = withContext(Dispatchers.IO) {
        val achievements = ensureAchievementsLoaded()
        val now = GameTimeProvider.getCurrentTimeMillis()
        val newlyCompleted = mutableListOf<AchievementConfig>()

        for (a in achievements) {
            if (a.isCompleted) continue
            val config = AchievementData.getConfig(a.achievementId) ?: continue
            if (config.type == event.type) {
                val newProgress = (a.currentProgress + event.amount).coerceAtMost(a.target)
                val isDone = newProgress >= a.target
                val updated = a.copy(
                    currentProgress = newProgress,
                    isCompleted = isDone,
                    updatedAt = now
                )
                achievementDao.insertOrUpdateAchievement(updated)

                if (isDone) {
                    newlyCompleted.add(config)
                }
            }
        }
        newlyCompleted
    }

    suspend fun claimAchievement(achievementId: String): Pair<Boolean, List<Reward>> = withContext(Dispatchers.IO) {
        val achievement = achievementDao.getAchievement(achievementId) ?: return@withContext Pair(false, emptyList())
        if (!achievement.isCompleted || achievement.isRewardClaimed) {
            return@withContext Pair(false, emptyList())
        }

        val config = AchievementData.getConfig(achievementId) ?: return@withContext Pair(false, emptyList())
        val granted = RewardManager.grantRewards(config.rewards)
        if (granted) {
            val updated = achievement.copy(
                isRewardClaimed = true,
                updatedAt = GameTimeProvider.getCurrentTimeMillis()
            )
            achievementDao.insertOrUpdateAchievement(updated)
            Pair(true, config.rewards)
        } else {
            Pair(false, emptyList())
        }
    }

    suspend fun hasUnclaimedCompletedAchievements(): Boolean = withContext(Dispatchers.IO) {
        val list = ensureAchievementsLoaded()
        list.any { it.isCompleted && !it.isRewardClaimed }
    }
}
