package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reward_state")
data class DailyRewardStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentDay: Int = 1,
    val lastClaimTimestamp: Long = 0L,
    val streakCount: Int = 0,
    val cycleStartTimestamp: Long = 0L
)
