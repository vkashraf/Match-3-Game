package com.example.game.island.building

import com.example.data.local.entity.BuildingEntity
import kotlin.math.min

object ProductionManager {

    const val MAX_STORED_HOURS = 8L
    const val MAX_STORED_MILLIS = MAX_STORED_HOURS * 3600 * 1000L

    /**
     * Calculates pending production in coins based on building level, config, and time elapsed.
     */
    fun calculatePendingCoins(
        building: BuildingEntity,
        currentTime: Long = System.currentTimeMillis()
    ): Long {
        if (!building.isBuilt || building.isConstructing) return 0L

        val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
        val ratePerHour = config.productionForLevel(building.level)
        if (ratePerHour <= 0) return 0L

        val elapsedMillis = (currentTime - building.lastCollectedAt).coerceAtLeast(0L)
        val clampedMillis = min(elapsedMillis, MAX_STORED_MILLIS)

        // Pending coins = (clampedMillis / 3600000.0) * ratePerHour
        return ((clampedMillis.toDouble() / 3600000.0) * ratePerHour).toLong()
    }

    /**
     * Checks if storage cap (8 hours) is reached.
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
     * Gets total max storage capacity in coins for a building.
     */
    fun getMaxStorageCapacity(building: BuildingEntity): Long {
        val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
        val ratePerHour = config.productionForLevel(building.level)
        return ratePerHour * MAX_STORED_HOURS
    }
}
