package com.example.game.match3.obstacle

data class Obstacle(
    val type: ObstacleType,
    var health: Int = 1,
    val maxHealth: Int = 1,
    val blocksMovement: Boolean = (type == ObstacleType.LOCKED || type == ObstacleType.VINE),
    val scoreReward: Int = when(type) {
        ObstacleType.CRATE -> 20
        ObstacleType.ICE -> 25
        ObstacleType.VINE -> 30
        ObstacleType.LOCKED -> 40
        else -> 0
    }
)
