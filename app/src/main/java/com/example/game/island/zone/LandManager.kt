package com.example.game.island.zone

import com.example.data.local.entity.IslandZoneEntity

object LandManager {

    fun canUnlockZone(zone: IslandZoneEntity, playerLevel: Int, totalStars: Int, coins: Long): Boolean {
        if (zone.isUnlocked) return false
        return playerLevel >= zone.requiredPlayerLevel &&
                totalStars >= zone.requiredStars &&
                coins >= zone.unlockCostCoins
    }

    fun unlockZone(
        zone: IslandZoneEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        ZoneManager.unlockZone(zone, onSuccess, onError)
    }

    fun isZoneUnlocked(zone: IslandZoneEntity): Boolean {
        return zone.isUnlocked
    }
}
