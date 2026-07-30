package com.example.game.island.model

enum class TerrainType(val id: String, val displayName: String, val isBuildable: Boolean) {
    GRASS("GRASS", "Grass", true),
    DIRT("DIRT", "Dirt", true),
    WATER("WATER", "Water", false),
    ROCK("ROCK", "Rock", false),
    SAND("SAND", "Sand", true),
    SNOW("SNOW", "Snow", true),
    PATH("PATH", "Cobblestone Path", true),
    BRIDGE("BRIDGE", "Bridge", true),
    DECORATIVE("DECORATIVE", "Decorative Soil", true);

    companion object {
        fun fromId(id: String): TerrainType {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GRASS
        }
    }
}
