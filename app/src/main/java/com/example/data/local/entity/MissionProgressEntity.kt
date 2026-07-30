package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey val missionId: String,
    val currentProgress: Int = 0,
    val target: Int = 1,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val isDaily: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
