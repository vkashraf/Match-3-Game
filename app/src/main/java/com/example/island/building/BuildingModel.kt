package com.example.island.building

enum class BuildingType {
    HOUSE,
    FARM,
    MINE,
    WORKSHOP,
    STORAGE,
    MARKET,
    HARBOR,
    CASTLE
}

data class BuildingModel(
    val id: String,
    val type: BuildingType,
    val name: String,
    val level: Int = 1,
    val maxLevel: Int = 10,
    val coinsProductionPerHour: Long = 60,
    val upgradeCostCoins: Long = 1000,
    val upgradeCostGems: Int = 10,
    val isUnlocked: Boolean = true
)
