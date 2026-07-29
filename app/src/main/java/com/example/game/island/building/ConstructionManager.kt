package com.example.game.island.building

import com.example.data.local.entity.BuildingEntity
import kotlin.math.ceil
import kotlin.math.max

object ConstructionManager {

    /**
     * Checks if construction timer has completed and returns updated BuildingEntity if changed.
     */
    fun checkAndFinishConstruction(building: BuildingEntity, currentTime: Long = System.currentTimeMillis()): BuildingEntity? {
        if (!building.isConstructing) return null
        if (currentTime >= building.constructionEndTime) {
            return building.copy(
                isConstructing = false,
                isBuilt = true,
                lastCollectedAt = currentTime
            )
        }
        return null
    }

    /**
     * Calculates current construction progress (0.0f to 1.0f).
     */
    fun getProgressFraction(building: BuildingEntity, currentTime: Long = System.currentTimeMillis()): Float {
        if (!building.isConstructing) return 1.0f
        val totalDuration = building.constructionEndTime - building.constructionStartTime
        if (totalDuration <= 0) return 1.0f
        val elapsed = currentTime - building.constructionStartTime
        return (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.0f, 1.0f)
    }

    /**
     * Calculates remaining time in seconds.
     */
    fun getRemainingSeconds(building: BuildingEntity, currentTime: Long = System.currentTimeMillis()): Long {
        if (!building.isConstructing) return 0L
        val remainingMillis = building.constructionEndTime - currentTime
        return max(0L, remainingMillis / 1000L)
    }

    /**
     * Calculates gem cost to finish construction instantly.
     */
    fun getInstantFinishGemCost(building: BuildingEntity, currentTime: Long = System.currentTimeMillis()): Int {
        val remainingSecs = getRemainingSeconds(building, currentTime)
        if (remainingSecs <= 0) return 0
        // Base cost: 5 gems per 60 seconds remaining, min 5 gems
        val gems = ceil(remainingSecs / 60.0 * 5.0).toInt()
        return max(5, gems)
    }

    /**
     * Formats seconds into MM:SS string.
     */
    fun formatRemainingTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
