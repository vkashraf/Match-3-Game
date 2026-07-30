package com.example.game.island.building

enum class BuildingType(val typeId: String, val displayName: String) {
    MAIN_HOUSE("MAIN_HOUSE", "Main House"),
    HOUSE("HOUSE", "Town House"),
    WOOD_HUT("WOOD_HUT", "Wood Hut"),
    STONE_WORKSHOP("STONE_WORKSHOP", "Stone Workshop"),
    FOOD_FARM("FOOD_FARM", "Food Farm"),
    METAL_WORKSHOP("METAL_WORKSHOP", "Metal Workshop"),
    FARM("FARM", "Windmill Farm"),
    MINE("MINE", "Quarry Mine"),
    WORKSHOP("WORKSHOP", "Craft Workshop"),
    STORAGE("STORAGE", "Island Storage"),
    BAKERY("BAKERY", "Island Bakery"),
    MARKET("MARKET", "Trade Market"),
    DECORATION_SHOP("DECORATION_SHOP", "Decor Shop"),
    ENERGY_HOUSE("ENERGY_HOUSE", "Energy Station"),
    SPECIAL_BUILDING("SPECIAL_BUILDING", "Monument"),
    LUMBER_MILL("LUMBER_MILL", "Lumber Mill"),
    HARBOR("HARBOR", "Island Harbor"),
    LABORATORY("LABORATORY", "Alchemy Lab"),
    MAGIC_TOWER("MAGIC_TOWER", "Wizard Tower");

    companion object {
        fun fromId(id: String): BuildingType {
            return values().firstOrNull { it.typeId.equals(id, ignoreCase = true) } ?: HOUSE
        }
    }
}
