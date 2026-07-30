package com.example.data.repository

import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.data.local.dao.WorldProgressDao
import com.example.data.local.entity.LevelProgressEntity
import com.example.data.local.entity.WorldProgressEntity
import com.example.game.world.WorldConfigRepository
import kotlinx.coroutines.flow.Flow

class WorldRepository(
    private val worldProgressDao: WorldProgressDao
) {

    val allWorldsFlow: Flow<List<WorldProgressEntity>> = worldProgressDao.getAllWorlds()

    suspend fun getWorldProgress(worldId: Int): WorldProgressEntity? {
        return worldProgressDao.getWorld(worldId)
    }

    suspend fun unlockWorld(worldId: Int) {
        worldProgressDao.unlockWorld(worldId)
        GameEventBus.postEvent(
            GameEvent(
                type = GameEventType.WORLD_UNLOCKED,
                worldId = worldId
            )
        )
    }

    suspend fun claimWorldReward(worldId: Int): Boolean {
        val world = worldProgressDao.getWorld(worldId) ?: return false
        if (!world.rewardClaimed && world.isCompleted) {
            worldProgressDao.claimReward(worldId)
            return true
        }
        return false
    }

    suspend fun updateWorldCompleted(worldId: Int) {
        val world = worldProgressDao.getWorld(worldId) ?: return
        if (!world.isCompleted) {
            val updated = world.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            worldProgressDao.updateWorld(updated)
            GameEventBus.postEvent(
                GameEvent(
                    type = GameEventType.WORLD_COMPLETED,
                    worldId = worldId
                )
            )

            // Unlock next world if exists
            if (worldId < WorldConfigRepository.TOTAL_WORLDS) {
                unlockWorld(worldId + 1)
            }
        }
    }

    suspend fun checkAndUpdateWorldProgress(
        worldId: Int,
        worldLevels: List<LevelProgressEntity>,
        totalStars: Int
    ) {
        val world = worldProgressDao.getWorld(worldId) ?: return
        val config = WorldConfigRepository.getWorld(worldId)

        // Check if unlocked by star requirement
        if (!world.isUnlocked && totalStars >= config.requiredStars) {
            unlockWorld(worldId)
        }

        // Check completion (all 10 levels completed)
        val completedCount = worldLevels.count { it.isCompleted }
        if (completedCount >= WorldConfigRepository.LEVELS_PER_WORLD) {
            updateWorldCompleted(worldId)
        }
    }
}
