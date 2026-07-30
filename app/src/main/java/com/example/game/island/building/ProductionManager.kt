package com.example.game.island.building

import com.example.data.local.entity.BuildingEntity
import com.example.game.resource.ResourceType
import kotlin.math.min

object ProductionManager {

    const val MAX_STORED_HOURS = 4L
    const val MAX_STORED_MILLIS = MAX_STORED_HOURS * 3600 * 1000L

    fun getProductionType(building: BuildingEntity): ResourceType {
        val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
        return config.productionType
    }

    /**
     * Calculates pending production amount based on building level, config, and time elapsed.
     */
    fun calculatePendingAmount(
        building: BuildingEntity,
        currentTime: Long = System.currentTimeMillis()
    ): Int {
        if (!building.isBuilt || building.isConstructing) return 0

        val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
        val ratePerHour = config.productionForLevel(building.level)
        if (ratePerHour <= 0) return 0

        val elapsedMillis = (currentTime - building.lastCollectedAt).coerceAtLeast(0L)
        val clampedMillis = min(elapsedMillis, MAX_STORED_MILLIS)

        return ((clampedMillis.toDouble() / 3600000.0) * ratePerHour).toInt()
    }

    fun calculatePendingCoins(
        building: BuildingEntity,
        currentTime: Long = System.currentTimeMillis()
    ): Long {
        val amount = calculatePendingAmount(building, currentTime)
        val type = getProductionType(building)
        return if (type == ResourceType.COINS) amount.toLong() else 0L
    }

    /**
     * Checks if storage cap is reached.
     */
    fun isStorageFull(
        building: BuildingEntity,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (!building.isBuilt || building.isConstructing) return false
        val elapsedMillis = (currentTime - building.lastCollectedAt).coerceAtLeast(0L)
        return elapsedMillis >= MAX_STORED_MILLIS
    }

    /**
     * Gets total max storage capacity for a building's production.
     */
    fun getMaxStorageCapacity(building: BuildingEntity): Long {
        val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
        val ratePerHour = config.productionForLevel(building.level)
        return ratePerHour * MAX_STORED_HOURS
    }
}
