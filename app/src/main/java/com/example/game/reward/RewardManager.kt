package com.example.game.reward

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object RewardManager {

    private val claimedReferences = ConcurrentHashMap.newKeySet<String>()

    suspend fun grantReward(reward: Reward, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (reward.quantity <= 0) return@withContext false
        if (referenceId != null) {
            if (!claimedReferences.add(referenceId)) {
                return@withContext false // duplicate protection
            }
        }

        val playerRepo = GameDataProvider.playerRepository
        val inventoryRepo = GameDataProvider.inventoryRepository

        when (reward.type) {
            RewardType.COINS -> {
                playerRepo.addCoins(reward.quantity.toLong())
            }
            RewardType.GEMS -> {
                playerRepo.addGems(reward.quantity)
            }
            RewardType.ENERGY -> {
                playerRepo.addEnergy(reward.quantity)
            }
            RewardType.BOOSTER -> {
                val boosterId = reward.itemId ?: "HAMMER"
                inventoryRepo.addItem(boosterId, reward.quantity, "BOOSTER")
            }
            RewardType.XP -> {
                playerRepo.addXp(reward.quantity.toLong())
            }
            RewardType.WOOD -> {
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.WOOD, reward.quantity, ignoreCapacity = true)
            }
            RewardType.STONE -> {
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.STONE, reward.quantity, ignoreCapacity = true)
            }
            RewardType.METAL -> {
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.METAL, reward.quantity, ignoreCapacity = true)
            }
            RewardType.FOOD -> {
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.FOOD, reward.quantity, ignoreCapacity = true)
            }
            RewardType.MATERIAL -> {
                val matId = reward.itemId ?: "WOOD"
                val resType = com.example.game.resource.ResourceType.fromId(matId)
                com.example.game.resource.ResourceManager.addResource(resType, reward.quantity, ignoreCapacity = true)
            }
        }

        GameEventBus.emit(GameEvent(GameEventType.REWARD_GRANTED, amount = reward.quantity, itemId = reward.itemId))
        true
    }

    suspend fun grantRewards(rewards: List<Reward>, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (referenceId != null) {
            if (!claimedReferences.add(referenceId)) {
                return@withContext false
            }
        }
        var success = true
        for (reward in rewards) {
            val res = grantReward(reward)
            if (!res) success = false
        }
        success
    }

    suspend fun grantRewardBundle(bundle: RewardBundle): Boolean = withContext(Dispatchers.IO) {
        grantRewards(bundle.rewards, bundle.referenceId)
    }
}
