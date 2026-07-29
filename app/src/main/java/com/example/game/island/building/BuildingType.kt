package com.example.game.island.building

enum class BuildingType(val typeId: String, val displayName: String) {
    HOUSE("HOUSE", "Town House"),
    FARM("FARM", "Windmill Farm"),
    MINE("MINE", "Gold Mine"),
    WORKSHOP("WORKSHOP", "Craft Workshop"),
    BAKERY("BAKERY", "Island Bakery"),
    MARKET("MARKET", "Trade Market"),
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
