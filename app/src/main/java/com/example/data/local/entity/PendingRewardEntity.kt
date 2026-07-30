package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_rewards")
data class PendingRewardEntity(
    @PrimaryKey
    val pendingRewardId: String,
    val sourceType: String,
    val rewardType: String,
    val itemId: String? = null,
    val amount: Int,
    val referenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isClaimed: Boolean = false
)
