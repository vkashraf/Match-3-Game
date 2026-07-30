package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalLevelsPlayed: Int = 0,
    val totalLevelsWon: Int = 0,
    val totalLevelsLost: Int = 0,
    val totalTilesCleared: Long = 0L,
    val totalSpecialTilesCreated: Int = 0,
    val totalSpecialTilesActivated: Int = 0,
    val totalObstaclesDestroyed: Int = 0,
    val totalBoostersUsed: Int = 0,
    val totalCoinsEarned: Long = 0L,
    val totalGemsEarned: Int = 0,
    val totalBuildingsBuilt: Int = 0,
    val totalBuildingsUpgraded: Int = 0,
    val totalStarsEarned: Int = 0,
    val longestDailyStreak: Int = 0
)
