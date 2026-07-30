package com.example.data.repository

import com.example.core.event.GameEventType
import com.example.game.reward.Reward
import com.example.game.reward.RewardType

data class MissionConfig(
    val missionId: String,
    val title: String,
    val description: String,
    val type: GameEventType,
    val target: Int,
    val rewards: List<Reward>,
    val isDaily: Boolean = true
)

object MissionData {
    val DAILY_MISSIONS = listOf(
        MissionConfig(
            missionId = "daily_win_levels",
            title = "Level Conqueror",
            description = "Win 2 match-3 levels",
            type = GameEventType.LEVEL_COMPLETED,
            target = 2,
            rewards = listOf(
                Reward(RewardType.COINS, quantity = 100),
                Reward(RewardType.XP, quantity = 50)
            )
        ),
        MissionConfig(
            missionId = "daily_destroy_obstacles",
            title = "Obstacle Breaker",
            description = "Destroy 20 board obstacles",
            type = GameEventType.OBSTACLE_DESTROYED,
            target = 20,
            rewards = listOf(
                Reward(RewardType.COINS, quantity = 150),
                Reward(RewardType.XP, quantity = 50)
            )
        ),
        MissionConfig(
            missionId = "daily_use_booster",
            title = "Power User",
            description = "Use 1 booster during gameplay",
            type = GameEventType.BOOSTER_USED,
            target = 1,
            rewards = listOf(
                Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 1),
                Reward(RewardType.XP, quantity = 50)
            )
        )
    )

    val WEEKLY_MISSIONS = listOf(
        MissionConfig(
            missionId = "weekly_win_levels",
            title = "Grand Conqueror",
            description = "Win 15 match-3 levels",
            type = GameEventType.LEVEL_COMPLETED,
            target = 15,
            rewards = listOf(
                Reward(RewardType.COINS, quantity = 500),
                Reward(RewardType.GEMS, quantity = 20),
                Reward(RewardType.XP, quantity = 200)
            ),
            isDaily = false
        ),
        MissionConfig(
            missionId = "weekly_collect_resources",
            title = "Master Harvester",
            description = "Collect 300 island resources",
            type = GameEventType.RESOURCE_COLLECTED,
            target = 300,
            rewards = listOf(
                Reward(RewardType.COINS, quantity = 400),
                Reward(RewardType.GEMS, quantity = 15),
                Reward(RewardType.XP, quantity = 150)
            ),
            isDaily = false
        ),
        MissionConfig(
            missionId = "weekly_build",
            title = "Master Builder",
            description = "Construct or upgrade 3 buildings",
            type = GameEventType.BUILDING_COMPLETED,
            target = 3,
            rewards = listOf(
                Reward(RewardType.COINS, quantity = 600),
                Reward(RewardType.GEMS, quantity = 25),
                Reward(RewardType.XP, quantity = 250)
            ),
            isDaily = false
        )
    )

    val ALL_MISSIONS = DAILY_MISSIONS + WEEKLY_MISSIONS

    fun getConfig(missionId: String): MissionConfig? {
        return ALL_MISSIONS.firstOrNull { it.missionId == missionId }
    }
}
