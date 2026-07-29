package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val playerId: Int = 1,
    val playerName: String = "Player",
    val playerLevel: Int = 1,
    val xp: Long = 0,
    val coins: Long = 500,
    val gems: Int = 50,
    val energy: Int = 5,
    val maxEnergy: Int = 5,
    val totalStars: Int = 0,
    val currentLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis()
)
