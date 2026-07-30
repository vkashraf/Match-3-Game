package com.example.game.world

import com.badlogic.gdx.graphics.Color

object WorldConfigRepository {

    private val worlds = listOf(
        WorldConfig(
            worldId = 1,
            worldName = "Meadow Village",
            theme = WorldTheme.MEADOW,
            bgTopColor = Color(0.35f, 0.72f, 0.42f, 1f),
            bgBottomColor = Color(0.2f, 0.55f, 0.32f, 1f),
            pathColor = Color(0.9f, 0.82f, 0.65f, 1f),
            islandColor = Color(0.48f, 0.82f, 0.48f, 1f),
            startLevel = 1,
            endLevel = 20,
            requiredStars = 0,
            rewardCoins = 500,
            rewardXp = 200,
            rewardGems = 10,
            description = "Peaceful grassy meadows and cozy countryside cottages."
        ),
        WorldConfig(
            worldId = 2,
            worldName = "Berry Forest",
            theme = WorldTheme.BERRY,
            bgTopColor = Color(0.18f, 0.52f, 0.35f, 1f),
            bgBottomColor = Color(0.1f, 0.38f, 0.25f, 1f),
            pathColor = Color(0.85f, 0.7f, 0.55f, 1f),
            islandColor = Color(0.28f, 0.62f, 0.38f, 1f),
            startLevel = 21,
            endLevel = 40,
            requiredStars = 15,
            rewardCoins = 600,
            rewardXp = 250,
            rewardGems = 12,
            description = "Lush forest trails filled with wild berry bushes and mushrooms."
        ),
        WorldConfig(
            worldId = 3,
            worldName = "Crystal Valley",
            theme = WorldTheme.CRYSTAL,
            bgTopColor = Color(0.22f, 0.18f, 0.45f, 1f),
            bgBottomColor = Color(0.12f, 0.1f, 0.3f, 1f),
            pathColor = Color(0.7f, 0.8f, 0.95f, 1f),
            islandColor = Color(0.35f, 0.28f, 0.62f, 1f),
            startLevel = 41,
            endLevel = 60,
            requiredStars = 30,
            rewardCoins = 700,
            rewardXp = 300,
            rewardGems = 15,
            description = "Mystical cavern valley glittering with glowing purple crystals."
        ),
        WorldConfig(
            worldId = 4,
            worldName = "Sunset Desert",
            theme = WorldTheme.DESERT,
            bgTopColor = Color(0.85f, 0.52f, 0.28f, 1f),
            bgBottomColor = Color(0.65f, 0.32f, 0.15f, 1f),
            pathColor = Color(0.95f, 0.88f, 0.65f, 1f),
            islandColor = Color(0.9f, 0.68f, 0.35f, 1f),
            startLevel = 61,
            endLevel = 80,
            requiredStars = 45,
            rewardCoins = 800,
            rewardXp = 350,
            rewardGems = 18,
            description = "Warm golden sand dunes bathed in glowing sunset hues."
        ),
        WorldConfig(
            worldId = 5,
            worldName = "Frozen Mountain",
            theme = WorldTheme.FROZEN,
            bgTopColor = Color(0.38f, 0.68f, 0.88f, 1f),
            bgBottomColor = Color(0.18f, 0.45f, 0.68f, 1f),
            pathColor = Color(0.85f, 0.95f, 1f, 1f),
            islandColor = Color(0.62f, 0.85f, 0.95f, 1f),
            startLevel = 81,
            endLevel = 100,
            requiredStars = 60,
            rewardCoins = 1000,
            rewardXp = 400,
            rewardGems = 20,
            description = "Majestic snowy peaks surrounded by crystalline ice glaciers."
        ),
        WorldConfig(
            worldId = 6,
            worldName = "Mystic Garden",
            theme = WorldTheme.MYSTIC,
            bgTopColor = Color(0.42f, 0.22f, 0.55f, 1f),
            bgBottomColor = Color(0.22f, 0.12f, 0.38f, 1f),
            pathColor = Color(0.9f, 0.75f, 0.95f, 1f),
            islandColor = Color(0.55f, 0.32f, 0.68f, 1f),
            startLevel = 101,
            endLevel = 120,
            requiredStars = 75,
            rewardCoins = 1200,
            rewardXp = 450,
            rewardGems = 22,
            description = "Enchanted botanical sanctuary with glowing moonlight blooms."
        ),
        WorldConfig(
            worldId = 7,
            worldName = "Cloud Kingdom",
            theme = WorldTheme.CLOUD,
            bgTopColor = Color(0.45f, 0.75f, 0.95f, 1f),
            bgBottomColor = Color(0.25f, 0.55f, 0.82f, 1f),
            pathColor = Color(1f, 0.98f, 0.88f, 1f),
            islandColor = Color(0.85f, 0.92f, 1f, 1f),
            startLevel = 121,
            endLevel = 140,
            requiredStars = 90,
            rewardCoins = 1500,
            rewardXp = 500,
            rewardGems = 25,
            description = "Heavenly sky islands floating above soft pastel clouds."
        ),
        WorldConfig(
            worldId = 8,
            worldName = "Volcano Valley",
            theme = WorldTheme.VOLCANO,
            bgTopColor = Color(0.68f, 0.22f, 0.18f, 1f),
            bgBottomColor = Color(0.38f, 0.12f, 0.1f, 1f),
            pathColor = Color(0.98f, 0.65f, 0.3f, 1f),
            islandColor = Color(0.48f, 0.22f, 0.18f, 1f),
            startLevel = 141,
            endLevel = 160,
            requiredStars = 105,
            rewardCoins = 1800,
            rewardXp = 550,
            rewardGems = 28,
            description = "Dramatic lava streams and rugged basalt stone bridges."
        ),
        WorldConfig(
            worldId = 9,
            worldName = "Magic Harbor",
            theme = WorldTheme.HARBOR,
            bgTopColor = Color(0.15f, 0.55f, 0.68f, 1f),
            bgBottomColor = Color(0.08f, 0.32f, 0.48f, 1f),
            pathColor = Color(0.9f, 0.82f, 0.65f, 1f),
            islandColor = Color(0.25f, 0.62f, 0.68f, 1f),
            startLevel = 161,
            endLevel = 180,
            requiredStars = 120,
            rewardCoins = 2000,
            rewardXp = 600,
            rewardGems = 30,
            description = "Bustling fantasy port with wooden docks and glowing lanterns."
        ),
        WorldConfig(
            worldId = 10,
            worldName = "Grand Fantasy Castle",
            theme = WorldTheme.CASTLE,
            bgTopColor = Color(0.28f, 0.22f, 0.48f, 1f),
            bgBottomColor = Color(0.15f, 0.12f, 0.32f, 1f),
            pathColor = Color(0.98f, 0.85f, 0.42f, 1f),
            islandColor = Color(0.42f, 0.35f, 0.62f, 1f),
            startLevel = 181,
            endLevel = 200,
            requiredStars = 135,
            rewardCoins = 2500,
            rewardXp = 700,
            rewardGems = 35,
            description = "Royal citadel featuring majestic towers and golden court gardens."
        )
    )

    fun getWorlds(): List<WorldConfig> = worlds

    fun getWorld(worldId: Int): WorldConfig {
        return worlds.firstOrNull { it.worldId == worldId } ?: worlds.first()
    }

    fun getWorldForLevel(levelId: Int): WorldConfig {
        val calculatedWorldId = ((levelId - 1) / LEVELS_PER_WORLD + 1).coerceIn(1, TOTAL_WORLDS)
        return getWorld(calculatedWorldId)
    }

    const val TOTAL_WORLDS = 10
    const val LEVELS_PER_WORLD = 20
    const val TOTAL_LEVELS = 200
}
