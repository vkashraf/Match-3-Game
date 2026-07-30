package com.example.data.repository

import com.example.core.event.GameEventType
import com.example.game.reward.Reward
import com.example.game.reward.RewardType

enum class AchievementCategory {
    LEVELS,
    MATCH,
    BUILDING,
    COLLECTION,
    SPECIAL
}

data class AchievementConfig(
    val achievementId: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val type: GameEventType,
    val target: Int,
    val rewards: List<Reward>
)

object AchievementData {
    val ACHIEVEMENTS = listOf(
        AchievementConfig(
            achievementId = "first_win",
            title = "First Victory",
            description = "Win 1 match-3 level",
            category = AchievementCategory.LEVELS,
            type = GameEventType.LEVEL_COMPLETED,
            target = 1,
            rewards = listOf(Reward(RewardType.COINS, quantity = 100), Reward(RewardType.XP, quantity = 50))
        ),
        AchievementConfig(
            achievementId = "win_10_levels",
            title = "Level Explorer",
            description = "Win 10 match-3 levels",
            category = AchievementCategory.LEVELS,
            type = GameEventType.LEVEL_COMPLETED,
            target = 10,
            rewards = listOf(Reward(RewardType.COINS, quantity = 300), Reward(RewardType.XP, quantity = 100))
        ),
        AchievementConfig(
            achievementId = "win_50_levels",
            title = "Level Master",
            description = "Win 50 match-3 levels",
            category = AchievementCategory.LEVELS,
            type = GameEventType.LEVEL_COMPLETED,
            target = 50,
            rewards = listOf(Reward(RewardType.COINS, quantity = 1000), Reward(RewardType.XP, quantity = 500))
        ),
        AchievementConfig(
            achievementId = "earn_1000_coins",
            title = "Coin Hoarder",
            description = "Earn 1,000 coins in total",
            category = AchievementCategory.COLLECTION,
            type = GameEventType.COINS_EARNED,
            target = 1000,
            rewards = listOf(Reward(RewardType.GEMS, quantity = 20), Reward(RewardType.XP, quantity = 100))
        ),
        AchievementConfig(
            achievementId = "destroy_100_obstacles",
            title = "Obstacle Breaker",
            description = "Destroy 100 board obstacles",
            category = AchievementCategory.MATCH,
            type = GameEventType.OBSTACLE_DESTROYED,
            target = 100,
            rewards = listOf(Reward(RewardType.COINS, quantity = 250), Reward(RewardType.BOOSTER, itemId = "HAMMER", quantity = 1))
        ),
        AchievementConfig(
            achievementId = "use_10_boosters",
            title = "Booster Expert",
            description = "Use 10 boosters",
            category = AchievementCategory.SPECIAL,
            type = GameEventType.BOOSTER_USED,
            target = 10,
            rewards = listOf(Reward(RewardType.COINS, quantity = 200), Reward(RewardType.BOOSTER, itemId = "SHUFFLE", quantity = 2))
        ),
        AchievementConfig(
            achievementId = "build_5_buildings",
            title = "Island Builder",
            description = "Construct or upgrade 5 island structures",
            category = AchievementCategory.BUILDING,
            type = GameEventType.BUILDING_BUILT,
            target = 5,
            rewards = listOf(Reward(RewardType.COINS, quantity = 500), Reward(RewardType.XP, quantity = 150))
        ),
        AchievementConfig(
            achievementId = "earn_10_stars",
            title = "Star Collector",
            description = "Earn 10 level stars",
            category = AchievementCategory.COLLECTION,
            type = GameEventType.STAR_EARNED,
            target = 10,
            rewards = listOf(Reward(RewardType.COINS, quantity = 300), Reward(RewardType.XP, quantity = 50))
        )
    )

    fun getConfig(achievementId: String): AchievementConfig? {
        return ACHIEVEMENTS.firstOrNull { it.achievementId == achievementId }
    }
}
