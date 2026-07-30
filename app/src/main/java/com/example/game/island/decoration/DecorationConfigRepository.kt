package com.example.game.island.decoration

object DecorationConfigRepository {

    private val configs = mapOf(
        "TREE_PALM" to DecorationConfig("TREE_PALM", "Palm Tree", "NATURE", 1, 1, 150L, 0, 1, "tree_palm", true),
        "TREE_OAK" to DecorationConfig("TREE_OAK", "Oak Tree", "NATURE", 1, 1, 200L, 0, 1, "tree_oak", true),
        "FLOWER_GARDEN" to DecorationConfig("FLOWER_GARDEN", "Flower Patch", "NATURE", 1, 1, 80L, 0, 1, "flower", false),
        "BUSH_LUSH" to DecorationConfig("BUSH_LUSH", "Green Bush", "NATURE", 1, 1, 50L, 0, 1, "bush", false),
        "FOUNTAIN_STONE" to DecorationConfig("FOUNTAIN_STONE", "Grand Fountain", "STRUCTURE", 2, 2, 1000L, 10, 3, "fountain", false),
        "BENCH_WOOD" to DecorationConfig("BENCH_WOOD", "Wooden Bench", "FURNITURE", 1, 1, 120L, 0, 2, "bench", true),
        "LAMP_POST" to DecorationConfig("LAMP_POST", "Street Lamp", "LIGHTING", 1, 1, 150L, 0, 2, "lamp", true),
        "STATUE_HERO" to DecorationConfig("STATUE_HERO", "Hero Statue", "SPECIAL", 2, 2, 2500L, 25, 5, "statue", false),
        "FLAG_ISLAND" to DecorationConfig("FLAG_ISLAND", "Island Flag", "STRUCTURE", 1, 1, 300L, 0, 2, "flag", true),
        "FENCE_WOOD" to DecorationConfig("FENCE_WOOD", "Wooden Fence", "STRUCTURE", 1, 1, 40L, 0, 1, "fence", true)
    )

    fun getConfig(id: String): DecorationConfig {
        return configs[id] ?: configs["TREE_PALM"]!!
    }

    fun getAllConfigs(): List<DecorationConfig> {
        return configs.values.toList()
    }
}
