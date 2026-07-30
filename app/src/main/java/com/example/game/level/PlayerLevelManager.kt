package com.example.game.level

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.data.repository.PlayerLevelConfig
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import com.example.game.unlock.UnlockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

data class LevelUpEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val rewards: List<Reward>
)

object PlayerLevelManager {

    private val _levelUpFlow = MutableSharedFlow<LevelUpEvent>(extraBufferCapacity = 16)
    val levelUpFlow: SharedFlow<LevelUpEvent> = _levelUpFlow.asSharedFlow()

    suspend fun addXpAndCheckLevelUp(xpAmount: Long): Boolean = withContext(Dispatchers.IO) {
        if (xpAmount <= 0) return@withContext false

        val playerRepo = GameDataProvider.playerRepository
        val player = playerRepo.getPlayer()

        val oldXp = player.xp
        val oldLevel = player.playerLevel
        val newXp = oldXp + xpAmount

        val newLevel = PlayerLevelConfig.getLevelForXp(newXp)

        playerRepo.addXp(xpAmount)
        GameEventBus.emit(GameEvent(GameEventType.XP_ADDED, amount = xpAmount.toInt()))

        if (newLevel > oldLevel) {
            val rewards = mutableListOf<Reward>()
            for (lvl in (oldLevel + 1)..newLevel) {
                rewards.addAll(LevelRewardConfig.getRewardsForLevel(lvl))
            }

            RewardManager.grantRewards(rewards, referenceId = "LEVEL_UP_${oldLevel}_TO_${newLevel}")

            GameEventBus.emit(
                GameEvent(
                    type = GameEventType.PLAYER_LEVEL_UP,
                    amount = newLevel,
                    levelId = newLevel
                )
            )

            UnlockManager.checkAndNotifyUnlocks(newLevel, player.totalStars)

            val event = LevelUpEvent(oldLevel, newLevel, rewards)
            _levelUpFlow.emit(event)
            return@withContext true
        }

        false
    }
}
