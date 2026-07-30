package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decorations")
data class DecorationEntity(
    @PrimaryKey val decorationInstanceId: String,
    val decorationId: String,
    val gridX: Int = 0,
    val gridY: Int = 0,
    val rotation: Int = 0,
    val plotId: String = "plot_starter"
)
