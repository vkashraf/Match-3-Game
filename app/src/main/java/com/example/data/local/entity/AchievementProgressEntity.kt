package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey val achievementId: String,
    val currentProgress: Int = 0,
    val target: Int = 1,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
