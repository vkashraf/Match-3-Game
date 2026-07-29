package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val buildingId: String,
    val buildingType: String,
    val plotId: String = "plot_starter",
    val level: Int = 1,
    val isBuilt: Boolean = true,
    val isConstructing: Boolean = false,
    val constructionStartTime: Long = 0L,
    val constructionEndTime: Long = 0L,
    val productionPerHour: Long = 60L,
    val lastCollectedAt: Long = System.currentTimeMillis()
)
