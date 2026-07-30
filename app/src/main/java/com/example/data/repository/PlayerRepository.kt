package com.example.data.repository

import com.example.data.local.dao.PlayerDao
import com.example.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val playerDao: PlayerDao) {

    val playerFlow: Flow<PlayerEntity?> = playerDao.observePlayer()

    suspend fun getPlayer(): PlayerEntity {
        return playerDao.getPlayer() ?: run {
            val defaultPlayer = PlayerEntity(
                playerId = 1,
                playerName = "Player",
                playerLevel = 1,
                xp = 0,
                coins = 500,
                gems = 50,
                energy = 5,
                maxEnergy = 5,
                totalStars = 0,
                currentLevel = 1
            )
            playerDao.insertPlayer(defaultPlayer)
            defaultPlayer
        }
    }

    suspend fun addCoins(amount: Long): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        val newCoins = p.coins + amount
        playerDao.updateCoins(newCoins)
        return true
    }

    suspend fun spendCoins(amount: Long): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        if (p.coins < amount) return false
        val newCoins = p.coins - amount
        playerDao.updateCoins(newCoins)
        return true
    }

    suspend fun addGems(amount: Int): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        val newGems = p.gems + amount
        playerDao.updateGems(newGems)
        return true
    }

    suspend fun spendGems(amount: Int): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        if (p.gems < amount) return false
        val newGems = p.gems - amount
        playerDao.updateGems(newGems)
        return true
    }

    suspend fun addEnergy(amount: Int): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        val newEnergy = (p.energy + amount).coerceAtMost(p.maxEnergy)
        playerDao.updateEnergy(newEnergy)
        return true
    }

    suspend fun spendEnergy(amount: Int): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        if (p.energy < amount) return false
        val newEnergy = p.energy - amount
        playerDao.updateEnergy(newEnergy)
        return true
    }

    suspend fun addXp(amount: Long): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        val oldLevel = p.playerLevel
        val newXp = p.xp + amount
        val newLevel = PlayerLevelConfig.getLevelForXp(newXp)
        playerDao.updateXpAndLevel(newXp, newLevel)

        if (newLevel > oldLevel) {
            for (lvl in (oldLevel + 1)..newLevel) {
                val rewards = PlayerLevelConfig.getLevelUpRewards(lvl)
                com.example.game.reward.RewardManager.grantRewards(rewards)
                com.example.core.event.GameEventBus.emit(
                    com.example.core.event.GameEvent(
                        com.example.core.event.GameEventType.PLAYER_LEVEL_UP,
                        amount = lvl
                    )
                )
            }
        }
        return true
    }

    suspend fun addStars(amount: Int): Boolean {
        if (amount <= 0) return false
        val p = getPlayer()
        val newStars = p.totalStars + amount
        playerDao.updateStars(newStars)
        return true
    }

    suspend fun updateCurrentLevel(levelId: Int): Boolean {
        if (levelId < 1) return false
        playerDao.updateCurrentLevel(levelId)
        return true
    }
}
