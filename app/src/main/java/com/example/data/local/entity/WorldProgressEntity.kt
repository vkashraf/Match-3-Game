package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_progress")
data class WorldProgressEntity(
    @PrimaryKey val worldId: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val rewardClaimed: Boolean = false,
    val unlockedAt: Long = 0,
    val completedAt: Long = 0
)
