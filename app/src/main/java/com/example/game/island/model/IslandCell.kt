package com.example.game.island.model

data class IslandCell(
    val row: Int,
    val column: Int,
    var terrainType: TerrainType = TerrainType.GRASS,
    var isUnlocked: Boolean = true,
    var buildingId: String? = null,
    var decorationId: String? = null,
    var isOccupied: Boolean = false,
    var isBuildable: Boolean = true
)
