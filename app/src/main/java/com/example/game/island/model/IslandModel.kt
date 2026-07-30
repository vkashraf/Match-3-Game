package com.example.game.island.model

data class IslandModel(
    val islandId: String = "main_island",
    val name: String = "Paradise Island",
    val width: Int = 20,
    val height: Int = 20,
    val theme: String = "DEFAULT",
    val unlockedArea: Int = 1,
    val playerLevel: Int = 1,
    val backgroundId: String = "bg_tropical"
)
