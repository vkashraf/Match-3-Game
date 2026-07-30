package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val bestMoves: Int = 0,
    val completedAt: Long = 0,
    val timesPlayed: Int = 0,
    val timesWon: Int = 0,
    val timesLost: Int = 0,
    val lastPlayedAt: Long = 0
)
