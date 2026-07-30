package com.example.game.match3.obstacle

data class Obstacle(
    val type: ObstacleType,
    var health: Int = if (type == ObstacleType.DOUBLE_CRATE) 2 else 1,
    val maxHealth: Int = if (type == ObstacleType.DOUBLE_CRATE) 2 else 1,
    var remainingMoves: Int = 10,
    val portalId: String? = null,
    val blocksMovement: Boolean = (type == ObstacleType.LOCKED || type == ObstacleType.LOCK || type == ObstacleType.CHAIN || type == ObstacleType.VINE || type == ObstacleType.STONE),
    val scoreReward: Int = when(type) {
        ObstacleType.CRATE -> 20
        ObstacleType.DOUBLE_CRATE -> 40
        ObstacleType.ICE -> 25
        ObstacleType.CHAIN -> 30
        ObstacleType.JELLY -> 20
        ObstacleType.VINE -> 30
        ObstacleType.LOCK, ObstacleType.LOCKED -> 40
        ObstacleType.STONE -> 50
        ObstacleType.BOMB -> 60
        else -> 0
    }
)
