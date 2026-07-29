package com.example.game.island.building

import kotlin.math.pow

data class BuildingConfig(
    val buildingType: BuildingType,
    val maxLevel: Int = 10,
    val baseCost: Long,
    val growthMultiplier: Double = 1.8,
    val baseConstructionDurationSecs: Int,
    val baseProductionPerHour: Long,
    val requiredPlayerLevel: Int = 1,
    val requiredStars: Int = 0
) {
    fun costForLevel(level: Int): Long {
        if (level <= 1) return baseCost
        val factor = growthMultiplier.pow((level - 1).toDouble())
        return (baseCost * factor).toLong()
    }

    fun productionForLevel(level: Int): Long {
        if (level <= 0) return 0L
        val factor = 1.5.pow((level - 1).toDouble())
        return (baseProductionPerHour * factor).toLong()
    }

    fun constructionDurationSecs(level: Int): Int {
        if (level <= 1) return baseConstructionDurationSecs
        val factor = 1.4.pow((level - 1).toDouble())
        return (baseConstructionDurationSecs * factor).toInt()
    }
}
