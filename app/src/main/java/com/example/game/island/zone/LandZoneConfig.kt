package com.example.game.island.zone

data class LandZoneConfig(
    val zoneId: Int,
    val name: String,
    val gridStartX: Int,
    val gridStartY: Int,
    val width: Int,
    val height: Int,
    val unlockCostCoins: Long,
    val requiredLevel: Int,
    val requiredWorld: Int = 1,
    val requiredStars: Int = 0
)
