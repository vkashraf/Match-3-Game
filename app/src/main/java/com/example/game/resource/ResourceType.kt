package com.example.game.resource

enum class ResourceType(val id: String, val displayName: String, val iconName: String) {
    COINS("COINS", "Coins", "coin"),
    GEMS("GEMS", "Gems", "gem"),
    ENERGY("ENERGY", "Energy", "energy"),
    WOOD("WOOD", "Wood", "wood"),
    STONE("STONE", "Stone", "stone"),
    METAL("METAL", "Metal", "metal"),
    FOOD("FOOD", "Food", "food"),
    XP("XP", "XP", "xp");

    companion object {
        fun fromId(id: String): ResourceType {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: WOOD
        }
    }
}
