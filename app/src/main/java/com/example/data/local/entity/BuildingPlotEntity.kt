package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building_plots")
data class BuildingPlotEntity(
    @PrimaryKey val plotId: String,
    val zoneId: Int,
    val x: Float,
    val y: Float,
    val isUnlocked: Boolean = false,
    val buildingId: String? = null,
    val requiredPlayerLevel: Int = 1,
    val requiredStars: Int = 0,
    val unlockCostCoins: Long = 0L
)
