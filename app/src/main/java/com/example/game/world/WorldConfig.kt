package com.example.game.world

import com.badlogic.gdx.graphics.Color

enum class WorldTheme(val themeId: String) {
    MEADOW("MEADOW"),
    BERRY("BERRY"),
    CRYSTAL("CRYSTAL"),
    DESERT("DESERT"),
    FROZEN("FROZEN"),
    MYSTIC("MYSTIC"),
    CLOUD("CLOUD"),
    VOLCANO("VOLCANO"),
    HARBOR("HARBOR"),
    CASTLE("CASTLE")
}

data class WorldConfig(
    val worldId: Int,
    val worldName: String,
    val theme: WorldTheme,
    val bgTopColor: Color,
    val bgBottomColor: Color,
    val pathColor: Color,
    val islandColor: Color,
    val startLevel: Int,
    val endLevel: Int,
    val requiredStars: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val rewardGems: Int,
    val description: String
)
