package com.example.model

import com.example.utils.Constants

data class PlayerData(
    val level: Int = 1,
    val xp: Long = 0,
    val coins: Long = Constants.DEFAULT_COINS.toLong(),
    val gems: Int = Constants.DEFAULT_GEMS,
    val energy: Int = Constants.DEFAULT_ENERGY,
    val totalStars: Int = 0,
    val currentLevelId: Int = 1
)
