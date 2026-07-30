package com.example.data.repository

import com.example.game.reward.Reward
import com.example.game.reward.RewardType

data class DailyRewardConfig(
    val day: Int,
    val title: String,
    val rewards: List<Reward>
)

object DailyRewardData {
    val REWARDS = listOf(
        DailyRewardConfig(1, "Day 1", listOf(Reward(RewardType.COINS, quantity = 100))),
        DailyRewardConfig(2, "Day 2", listOf(Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 1))),
        DailyRewardConfig(3, "Day 3", listOf(Reward(RewardType.COINS, quantity = 150))),
        DailyRewardConfig(4, "Day 4", listOf(Reward(RewardType.BOOSTER, itemId = "SHUFFLE", quantity = 1))),
        DailyRewardConfig(5, "Day 5", listOf(Reward(RewardType.COINS, quantity = 250))),
        DailyRewardConfig(6, "Day 6", listOf(
            Reward(RewardType.BOOSTER, itemId = "SWAP", quantity = 1),
            Reward(RewardType.BOOSTER, itemId = "EXTRA_MOVES", quantity = 1)
        )),
        DailyRewardConfig(7, "Day 7", listOf(
            Reward(RewardType.COINS, quantity = 500),
            Reward(RewardType.XP, quantity = 100),
            Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 1)
        ))
    )

    fun getForDay(day: Int): DailyRewardConfig {
        val cycleDay = ((day - 1) % 7) + 1
        return REWARDS.firstOrNull { it.day == cycleDay } ?: REWARDS.first()
    }
}
