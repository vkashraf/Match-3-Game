package com.example.game.level

import com.example.game.reward.Reward
import com.example.game.reward.RewardType

object LevelRewardConfig {

    fun getRewardsForLevel(newLevel: Int): List<Reward> {
        val coinAmount = 200 + (newLevel * 50)
        val gemAmount = 5 + (newLevel / 2)
        val energyAmount = 2

        val rewards = mutableListOf(
            Reward(RewardType.COINS, quantity = coinAmount),
            Reward(RewardType.GEMS, quantity = gemAmount),
            Reward(RewardType.ENERGY, quantity = energyAmount)
        )

        // Milestone bonus rewards
        when {
            newLevel == 5 -> {
                rewards.add(Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 2))
                rewards.add(Reward(RewardType.GEMS, quantity = 20))
            }
            newLevel == 10 -> {
                rewards.add(Reward(RewardType.BOOSTER, itemId = "BOMB", quantity = 3))
                rewards.add(Reward(RewardType.GEMS, quantity = 50))
            }
            newLevel % 10 == 0 -> {
                rewards.add(Reward(RewardType.BOOSTER, itemId = "COLOR_BOMB", quantity = 2))
                rewards.add(Reward(RewardType.GEMS, quantity = 30))
            }
            newLevel % 2 == 0 -> {
                rewards.add(Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 1))
            }
            else -> {
                rewards.add(Reward(RewardType.BOOSTER, itemId = "SWAP", quantity = 1))
            }
        }

        return rewards
    }
}
