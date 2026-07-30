package com.example.data.repository

object PlayerLevelConfig {

    /**
     * Required cumulative XP to reach a given player level.
     */
    fun getRequiredXpForLevel(level: Int): Long {
        if (level <= 1) return 0L
        val lvl = level - 1
        return (lvl * 100L) + (lvl * (lvl - 1) * 50L)
    }

    /**
     * Calculates the level based on total accumulated XP.
     */
    fun getLevelForXp(totalXp: Long): Int {
        var level = 1
        while (totalXp >= getRequiredXpForLevel(level + 1)) {
            level++
        }
        return level
    }

    /**
     * Gets rewards earned when leveling up to a specific level.
     */
    fun getLevelUpRewards(newLevel: Int): List<com.example.game.reward.Reward> {
        val coinAmount = 300 + (newLevel * 100)
        val gemAmount = 5 + (newLevel * 2)
        val rewards = mutableListOf(
            com.example.game.reward.Reward(com.example.game.reward.RewardType.COINS, quantity = coinAmount),
            com.example.game.reward.Reward(com.example.game.reward.RewardType.GEMS, quantity = gemAmount),
            com.example.game.reward.Reward(com.example.game.reward.RewardType.ENERGY, quantity = 2)
        )
        if (newLevel % 2 == 0) {
            rewards.add(com.example.game.reward.Reward(com.example.game.reward.RewardType.BOOSTER, itemId = "HAMMER", quantity = 1))
        } else {
            rewards.add(com.example.game.reward.Reward(com.example.game.reward.RewardType.BOOSTER, itemId = "SWAP", quantity = 1))
        }
        return rewards
    }
}
