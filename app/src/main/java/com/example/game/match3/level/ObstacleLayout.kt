package com.example.game.match3.level

import com.example.game.match3.obstacle.ObstacleType

data class ObstacleLayout(
    val row: Int,
    val col: Int,
    val obstacleType: ObstacleType,
    val health: Int = 1
)
