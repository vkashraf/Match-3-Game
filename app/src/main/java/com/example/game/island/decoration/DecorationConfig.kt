package com.example.game.island.decoration

data class DecorationConfig(
    val decorationId: String,
    val name: String,
    val category: String = "NATURE", // NATURE, STRUCTURE, LIGHTING, FURNITURE, SPECIAL
    val width: Int = 1,
    val height: Int = 1,
    val costCoins: Long = 100L,
    val costGems: Int = 0,
    val unlockLevel: Int = 1,
    val assetId: String = "tree",
    val rotationAllowed: Boolean = true
)
