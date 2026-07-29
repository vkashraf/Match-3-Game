package com.example.game.island.building

object BuildingConfigRepository {

    private val configs = mapOf(
        BuildingType.HOUSE to BuildingConfig(
            buildingType = BuildingType.HOUSE,
            baseCost = 100L,
            growthMultiplier = 1.6,
            baseConstructionDurationSecs = 15,
            baseProductionPerHour = 10L,
            requiredPlayerLevel = 1,
            requiredStars = 0
        ),
        BuildingType.FARM to BuildingConfig(
            buildingType = BuildingType.FARM,
            baseCost = 250L,
            growthMultiplier = 1.7,
            baseConstructionDurationSecs = 30,
            baseProductionPerHour = 20L,
            requiredPlayerLevel = 1,
            requiredStars = 0
        ),
        BuildingType.MINE to BuildingConfig(
            buildingType = BuildingType.MINE,
            baseCost = 500L,
            growthMultiplier = 1.8,
            baseConstructionDurationSecs = 45,
            baseProductionPerHour = 35L,
            requiredPlayerLevel = 3,
            requiredStars = 10
        ),
        BuildingType.WORKSHOP to BuildingConfig(
            buildingType = BuildingType.WORKSHOP,
            baseCost = 1000L,
            growthMultiplier = 2.0,
            baseConstructionDurationSecs = 60,
            baseProductionPerHour = 50L,
            requiredPlayerLevel = 5,
            requiredStars = 20
        ),
        BuildingType.BAKERY to BuildingConfig(
            buildingType = BuildingType.BAKERY,
            baseCost = 1500L,
            growthMultiplier = 2.0,
            baseConstructionDurationSecs = 90,
            baseProductionPerHour = 75L,
            requiredPlayerLevel = 6,
            requiredStars = 25
        ),
        BuildingType.MARKET to BuildingConfig(
            buildingType = BuildingType.MARKET,
            baseCost = 2500L,
            growthMultiplier = 2.2,
            baseConstructionDurationSecs = 120,
            baseProductionPerHour = 100L,
            requiredPlayerLevel = 7,
            requiredStars = 30
        ),
        BuildingType.LUMBER_MILL to BuildingConfig(
            buildingType = BuildingType.LUMBER_MILL,
            baseCost = 3500L,
            growthMultiplier = 2.2,
            baseConstructionDurationSecs = 150,
            baseProductionPerHour = 150L,
            requiredPlayerLevel = 9,
            requiredStars = 40
        ),
        BuildingType.HARBOR to BuildingConfig(
            buildingType = BuildingType.HARBOR,
            baseCost = 5000L,
            growthMultiplier = 2.5,
            baseConstructionDurationSecs = 180,
            baseProductionPerHour = 200L,
            requiredPlayerLevel = 10,
            requiredStars = 45
        ),
        BuildingType.LABORATORY to BuildingConfig(
            buildingType = BuildingType.LABORATORY,
            baseCost = 7500L,
            growthMultiplier = 2.5,
            baseConstructionDurationSecs = 240,
            baseProductionPerHour = 300L,
            requiredPlayerLevel = 12,
            requiredStars = 50
        ),
        BuildingType.MAGIC_TOWER to BuildingConfig(
            buildingType = BuildingType.MAGIC_TOWER,
            baseCost = 10000L,
            growthMultiplier = 2.8,
            baseConstructionDurationSecs = 300,
            baseProductionPerHour = 500L,
            requiredPlayerLevel = 15,
            requiredStars = 75
        )
    )

    fun getConfig(type: BuildingType): BuildingConfig {
        return configs[type] ?: configs.values.first()
    }

    fun getAllConfigs(): List<BuildingConfig> = configs.values.toList()
}
