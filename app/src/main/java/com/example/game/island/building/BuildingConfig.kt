package com.example.game.island.building

import com.example.game.resource.ResourceType
import kotlin.math.pow

data class BuildingConfig(
    val buildingType: BuildingType,
    val description: String = "",
    val category: String = "PRODUCTION", // PRODUCTION, STORAGE, SPECIAL, DECORATION, UTILITY
    val width: Int = 2,
    val height: Int = 2,
    val maxLevel: Int = 10,
    val baseCostCoins: Long,
    val baseCostWood: Int = 0,
    val baseCostStone: Int = 0,
    val baseCostMetal: Int = 0,
    val growthMultiplier: Double = 1.8,
    val baseConstructionDurationSecs: Int,
    val productionType: ResourceType = ResourceType.COINS,
    val baseProductionPerHour: Long,
    val requiredPlayerLevel: Int = 1,
    val requiredStars: Int = 0,
    val requiredMainHouseLevel: Int = 1
) {
    fun costCoinsForLevel(level: Int): Long {
        if (level <= 1) return baseCostCoins
        val factor = growthMultiplier.pow((level - 1).toDouble())
        return (baseCostCoins * factor).toLong()
    }

    fun costWoodForLevel(level: Int): Int {
        if (level <= 1) return baseCostWood
        val factor = 1.5.pow((level - 1).toDouble())
        return (baseCostWood * factor).toInt()
    }

    fun costStoneForLevel(level: Int): Int {
        if (level <= 1) return baseCostStone
        val factor = 1.5.pow((level - 1).toDouble())
        return (baseCostStone * factor).toInt()
    }

    fun costMetalForLevel(level: Int): Int {
        if (level <= 1) return baseCostMetal
        val factor = 1.5.pow((level - 1).toDouble())
        return (baseCostMetal * factor).toInt()
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
