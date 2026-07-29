package com.example.game.match3.level

import com.example.game.match3.obstacle.ObstacleType
import com.example.game.match3.tile.TileType

object LevelConfigRepository {

    private val levelCache = mutableMapOf<Int, LevelConfig>()

    init {
        generateAllLevels()
    }

    private fun generateAllLevels() {
        for (id in 1..100) {
            val difficulty = when {
                id <= 10 -> Difficulty.EASY
                id <= 30 -> Difficulty.NORMAL
                id <= 60 -> Difficulty.HARD
                id <= 85 -> Difficulty.VERY_HARD
                else -> Difficulty.EXPERT
            }

            val moves = when (difficulty) {
                Difficulty.EASY -> 28 - (id / 3)
                Difficulty.NORMAL -> 26 - ((id - 10) / 4)
                Difficulty.HARD -> 24 - ((id - 30) / 6)
                Difficulty.VERY_HARD -> 22 - ((id - 60) / 8)
                Difficulty.EXPERT -> 20 - ((id - 85) / 10)
            }.coerceIn(16, 30)

            val goals = mutableListOf<LevelGoal>()
            val obstacleLayouts = mutableListOf<ObstacleLayout>()

            val baseTarget1 = (12 + id % 8 + id / 5).coerceAtMost(35)
            val baseTarget2 = (10 + (id * 3) % 7 + id / 6).coerceAtMost(30)

            when (id) {
                in 1..10 -> {
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    if (id >= 4) goals.add(LevelGoal("GREEN_LEAF", baseTarget2))
                }
                in 11..20 -> {
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    goals.add(LevelGoal("CRATE", 2 + id % 3))
                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.CRATE, 1))
                    if (id >= 15) {
                        obstacleLayouts.add(ObstacleLayout(5, 3, ObstacleType.CRATE, 1))
                        obstacleLayouts.add(ObstacleLayout(5, 4, ObstacleType.CRATE, 1))
                    }
                }
                in 21..30 -> {
                    goals.add(LevelGoal("RED_HEART", baseTarget1))
                    goals.add(LevelGoal("ICE", 2 + id % 3))
                    obstacleLayouts.add(ObstacleLayout(3, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 4, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 4, ObstacleType.ICE, 1))
                }
                in 31..50 -> {
                    goals.add(LevelGoal("PURPLE_GEM", baseTarget1))
                    goals.add(LevelGoal("VINE", 2 + (id % 4)))
                    goals.add(LevelGoal("CRATE", 2 + (id % 3)))

                    obstacleLayouts.add(ObstacleLayout(1, 1, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(1, 6, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(6, 1, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(6, 6, ObstacleType.CRATE, 1))

                    obstacleLayouts.add(ObstacleLayout(3, 2, ObstacleType.VINE, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 5, ObstacleType.VINE, 1))
                }
                in 51..70 -> {
                    goals.add(LevelGoal("YELLOW_STAR", baseTarget1))
                    goals.add(LevelGoal("LOCKED", 2 + (id % 3)))
                    goals.add(LevelGoal("ICE", 3))

                    obstacleLayouts.add(ObstacleLayout(2, 3, ObstacleType.LOCKED, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 4, ObstacleType.LOCKED, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 3, ObstacleType.LOCKED, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 4, ObstacleType.LOCKED, 1))

                    obstacleLayouts.add(ObstacleLayout(3, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 4, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 3, ObstacleType.ICE, 1))
                }
                else -> {
                    // 71..100 Endgame
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    goals.add(LevelGoal("CRATE", 4))
                    goals.add(LevelGoal("VINE", 3))

                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 2, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 5, ObstacleType.CRATE, 1))

                    obstacleLayouts.add(ObstacleLayout(3, 1, ObstacleType.VINE, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 6, ObstacleType.VINE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 1, ObstacleType.VINE, 1))
                }
            }

            val baseScore = 1000 + id * 150
            val config = LevelConfig(
                levelId = id,
                moves = moves,
                goals = goals,
                difficulty = difficulty,
                baseScoreTarget = baseScore,
                coinReward = 100 + id * 10,
                xpReward = 40 + id * 3,
                obstacleLayouts = obstacleLayouts
            )

            if (LevelConfigValidator.validate(config)) {
                levelCache[id] = config
            } else {
                levelCache[id] = LevelConfig(
                    levelId = id,
                    moves = 25,
                    goals = listOf(LevelGoal("BLUE_DROP", 15)),
                    difficulty = Difficulty.NORMAL,
                    baseScoreTarget = 1200
                )
            }
        }
    }

    fun getLevelConfig(levelId: Int): LevelConfig {
        return levelCache[levelId] ?: LevelConfig(
            levelId = levelId.coerceIn(1, 100),
            moves = 25,
            goals = listOf(LevelGoal("BLUE_DROP", 15)),
            difficulty = Difficulty.EASY,
            baseScoreTarget = 1000
        )
    }

    fun isLastLevel(levelId: Int): Boolean = levelId >= 100
}
