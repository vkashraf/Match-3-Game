package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "island_zones")
data class IslandZoneEntity(
    @PrimaryKey val zoneId: Int,
    val name: String,
    val isUnlocked: Boolean = false,
    val requiredPlayerLevel: Int = 1,
    val requiredStars: Int = 0,
    val requiredCompletedLevel: Int = 1,
    val unlockCostCoins: Long = 0L
)
