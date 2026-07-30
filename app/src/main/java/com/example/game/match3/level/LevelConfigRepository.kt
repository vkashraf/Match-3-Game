package com.example.game.match3.level

import com.example.game.match3.obstacle.ObstacleType
import com.example.game.match3.tile.TileType
import com.example.game.world.WorldConfigRepository

object LevelConfigRepository {

    private val levelCache = mutableMapOf<Int, LevelConfig>()

    init {
        generateAllLevels()
    }

    private fun generateAllLevels() {
        for (id in 1..WorldConfigRepository.TOTAL_LEVELS) {
            val difficulty = when {
                id <= 20 -> Difficulty.EASY
                id <= 60 -> Difficulty.NORMAL
                id <= 120 -> Difficulty.HARD
                id <= 170 -> Difficulty.VERY_HARD
                id <= 190 -> Difficulty.EXPERT
                else -> Difficulty.EXTREME
            }

            val moves = when (difficulty) {
                Difficulty.EASY -> 28 - (id / 5)
                Difficulty.NORMAL -> 26 - ((id - 20) / 8)
                Difficulty.HARD -> 24 - ((id - 60) / 10)
                Difficulty.VERY_HARD -> 22 - ((id - 120) / 12)
                Difficulty.EXPERT -> 20 - ((id - 170) / 15)
                Difficulty.EXTREME -> 18 - ((id - 190) / 20)
            }.coerceIn(15, 30)

            val goals = mutableListOf<LevelGoal>()
            val obstacleLayouts = mutableListOf<ObstacleLayout>()

            val baseTarget1 = (12 + id % 8 + id / 8).coerceAtMost(40)
            val baseTarget2 = (10 + (id * 3) % 7 + id / 10).coerceAtMost(35)

            val worldId = ((id - 1) / WorldConfigRepository.LEVELS_PER_WORLD) + 1

            when (worldId) {
                1 -> { // Meadow
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    if (id >= 5) goals.add(LevelGoal("GREEN_LEAF", baseTarget2))
                    if (id >= 12) {
                        goals.add(LevelGoal("CRATE", 2 + id % 3))
                        obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.CRATE, 1))
                        obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.CRATE, 1))
                    }
                }
                2 -> { // Forest
                    goals.add(LevelGoal("RED_HEART", baseTarget1))
                    goals.add(LevelGoal("CRATE", 3 + id % 4))
                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 3, ObstacleType.CRATE, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 4, ObstacleType.CRATE, 1))
                }
                3 -> { // Cavern Valley
                    goals.add(LevelGoal("PURPLE_GEM", baseTarget1))
                    goals.add(LevelGoal("ICE", 4 + id % 3))
                    obstacleLayouts.add(ObstacleLayout(3, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 4, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 4, ObstacleType.ICE, 1))
                }
                4 -> { // Desert
                    goals.add(LevelGoal("YELLOW_STAR", baseTarget1))
                    goals.add(LevelGoal("DOUBLE_CRATE", 3 + id % 3))
                    obstacleLayouts.add(ObstacleLayout(1, 1, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(1, 6, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(6, 1, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(6, 6, ObstacleType.DOUBLE_CRATE, 2))
                }
                5 -> { // Frozen Mountain
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    goals.add(LevelGoal("CHAIN", 4))
                    goals.add(LevelGoal("ICE", 4))
                    obstacleLayouts.add(ObstacleLayout(2, 3, ObstacleType.CHAIN, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 4, ObstacleType.CHAIN, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 3, ObstacleType.CHAIN, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 4, ObstacleType.CHAIN, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 3, ObstacleType.ICE, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 4, ObstacleType.ICE, 1))
                }
                6 -> { // Mystic Garden
                    goals.add(LevelGoal("GREEN_LEAF", baseTarget1))
                    goals.add(LevelGoal("JELLY", 6))
                    obstacleLayouts.add(ObstacleLayout(3, 2, ObstacleType.JELLY, 1))
                    obstacleLayouts.add(ObstacleLayout(3, 5, ObstacleType.JELLY, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 2, ObstacleType.JELLY, 1))
                    obstacleLayouts.add(ObstacleLayout(4, 5, ObstacleType.JELLY, 1))
                }
                7 -> { // Cloud Kingdom
                    goals.add(LevelGoal("PURPLE_GEM", baseTarget1))
                    goals.add(LevelGoal("LOCK", 4))
                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.LOCK, 1))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.LOCK, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 2, ObstacleType.LOCK, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 5, ObstacleType.LOCK, 1))
                }
                8 -> { // Volcano Valley
                    goals.add(LevelGoal("RED_HEART", baseTarget1))
                    goals.add(LevelGoal("STONE", 3))
                    goals.add(LevelGoal("BOMB", 2))
                    obstacleLayouts.add(ObstacleLayout(1, 3, ObstacleType.STONE, 1))
                    obstacleLayouts.add(ObstacleLayout(1, 4, ObstacleType.STONE, 1))
                    obstacleLayouts.add(ObstacleLayout(6, 3, ObstacleType.BOMB, 1))
                    obstacleLayouts.add(ObstacleLayout(6, 4, ObstacleType.BOMB, 1))
                }
                9 -> { // Magic Harbor
                    goals.add(LevelGoal("BLUE_DROP", baseTarget1))
                    goals.add(LevelGoal("DOUBLE_CRATE", 4))
                    goals.add(LevelGoal("CHAIN", 4))
                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(5, 2, ObstacleType.CHAIN, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 5, ObstacleType.CHAIN, 1))
                }
                else -> { // Grand Fantasy Castle (World 10)
                    goals.add(LevelGoal("YELLOW_STAR", baseTarget1))
                    goals.add(LevelGoal("DOUBLE_CRATE", 4))
                    goals.add(LevelGoal("STONE", 4))
                    obstacleLayouts.add(ObstacleLayout(2, 2, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(2, 5, ObstacleType.DOUBLE_CRATE, 2))
                    obstacleLayouts.add(ObstacleLayout(5, 2, ObstacleType.STONE, 1))
                    obstacleLayouts.add(ObstacleLayout(5, 5, ObstacleType.STONE, 1))
                }
            }

            val baseScore = 1000 + id * 100
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
            levelId = levelId.coerceIn(1, WorldConfigRepository.TOTAL_LEVELS),
            moves = 25,
            goals = listOf(LevelGoal("BLUE_DROP", 15)),
            difficulty = Difficulty.EASY,
            baseScoreTarget = 1000
        )
    }

    fun isLastLevel(levelId: Int): Boolean = levelId >= WorldConfigRepository.TOTAL_LEVELS
}
