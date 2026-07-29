package com.example.game.match3.level

import com.example.game.match3.tile.TileType

data class LevelConfig(
    val levelId: Int,
    val moves: Int,
    val goals: List<LevelGoal>,
    val availableTileTypes: List<TileType> = listOf(
        TileType.BLUE_DROP,
        TileType.RED_HEART,
        TileType.GREEN_LEAF,
        TileType.YELLOW_STAR,
        TileType.PURPLE_GEM
    ),
    val difficulty: Difficulty = Difficulty.NORMAL,
    val baseScoreTarget: Int = 1000,
    val coinReward: Int = 100 + levelId * 10,
    val xpReward: Int = 40 + levelId * 3,
    val obstacleLayouts: List<ObstacleLayout> = emptyList()
)
