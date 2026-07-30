package com.example.game.shop.config

import com.example.game.match3.booster.BoosterType
import com.example.game.shop.model.PriceType
import com.example.game.shop.model.ShopBundleConfig
import com.example.game.shop.model.ShopItemConfig

object EconomyConfig {

    const val MAX_ENERGY = 5
    const val ENERGY_REGEN_MINUTES = 30
    const val LEVEL_ENERGY_COST = 1
    const val MAX_INVENTORY_QUANTITY = 99

    // Prices for single booster purchases
    val BOOSTER_SINGLE_PRICES = mapOf(
        BoosterType.HAMMER to Pair(PriceType.COINS, 100L),
        BoosterType.SWAP to Pair(PriceType.COINS, 150L),
        BoosterType.SHUFFLE to Pair(PriceType.COINS, 200L),
        BoosterType.EXTRA_MOVES to Pair(PriceType.GEMS, 100L),
        BoosterType.COLOR_BOMB to Pair(PriceType.GEMS, 150L),
        BoosterType.ROCKET_START to Pair(PriceType.GEMS, 120L),
        BoosterType.BOMB_START to Pair(PriceType.GEMS, 120L),
        BoosterType.RAINBOW_START to Pair(PriceType.GEMS, 200L)
    )

    // Energy item prices
    const val ENERGY_1_REFILL_GEMS = 10L
    const val FULL_ENERGY_REFILL_GEMS = 50L

    // Default shop items list
    fun getShopItems(): List<ShopItemConfig> {
        return listOf(
            ShopItemConfig(
                itemId = "HAMMER",
                itemType = "BOOSTER",
                displayName = "Hammer",
                description = "Destroys one selected tile or damages an obstacle.",
                iconName = "hammer",
                priceType = PriceType.COINS,
                priceAmount = 100L,
                quantity = 1,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "SWAP",
                itemType = "BOOSTER",
                displayName = "Free Swap",
                description = "Swaps any two adjacent tiles without making a match.",
                iconName = "swap",
                priceType = PriceType.COINS,
                priceAmount = 150L,
                quantity = 1,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "SHUFFLE",
                itemType = "BOOSTER",
                displayName = "Board Shuffle",
                description = "Rearranges all playable tiles on the board.",
                iconName = "shuffle",
                priceType = PriceType.COINS,
                priceAmount = 200L,
                quantity = 1,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "EXTRA_MOVES",
                itemType = "BOOSTER",
                displayName = "+5 Extra Moves",
                description = "Adds 5 extra moves to your current level.",
                iconName = "extra_moves",
                priceType = PriceType.GEMS,
                priceAmount = 100L,
                quantity = 1,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "ENERGY_1",
                itemType = "ENERGY",
                displayName = "+1 Energy",
                description = "Refills 1 point of energy.",
                iconName = "energy",
                priceType = PriceType.GEMS,
                priceAmount = ENERGY_1_REFILL_GEMS,
                quantity = 1,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "FULL_ENERGY",
                itemType = "ENERGY",
                displayName = "Full Energy Refill",
                description = "Instantly refills energy to max capacity (5/5).",
                iconName = "energy",
                priceType = PriceType.GEMS,
                priceAmount = FULL_ENERGY_REFILL_GEMS,
                quantity = 5,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "WOOD_50",
                itemType = "MATERIAL",
                displayName = "50 Wood",
                description = "Essential building material for island structures.",
                iconName = "wood",
                priceType = PriceType.COINS,
                priceAmount = 300L,
                quantity = 50,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "STONE_40",
                itemType = "MATERIAL",
                displayName = "40 Stone",
                description = "Durable stone for island upgrades.",
                iconName = "stone",
                priceType = PriceType.COINS,
                priceAmount = 300L,
                quantity = 40,
                unlockLevel = 1
            ),
            ShopItemConfig(
                itemId = "METAL_30",
                itemType = "MATERIAL",
                displayName = "30 Metal",
                description = "Refined metal for advanced constructions.",
                iconName = "metal",
                priceType = PriceType.COINS,
                priceAmount = 300L,
                quantity = 30,
                unlockLevel = 2
            ),
            ShopItemConfig(
                itemId = "FOOD_50",
                itemType = "MATERIAL",
                displayName = "50 Food",
                description = "Fresh produce collected for islanders.",
                iconName = "food",
                priceType = PriceType.COINS,
                priceAmount = 250L,
                quantity = 50,
                unlockLevel = 1
            )
        )
    }

    // Default shop bundles list
    fun getShopBundles(): List<ShopBundleConfig> {
        return listOf(
            ShopBundleConfig(
                bundleId = "HAMMER_PACK_3",
                itemId = "HAMMER",
                displayName = "Hammer Pack (x3)",
                quantity = 3,
                priceType = PriceType.COINS,
                priceAmount = 270L, // Discounted
                bonusQuantity = 0,
                unlockLevel = 1
            ),
            ShopBundleConfig(
                bundleId = "SWAP_PACK_3",
                itemId = "SWAP",
                displayName = "Swap Pack (x3)",
                quantity = 3,
                priceType = PriceType.COINS,
                priceAmount = 400L,
                bonusQuantity = 0,
                unlockLevel = 1
            ),
            ShopBundleConfig(
                bundleId = "SHUFFLE_PACK_5",
                itemId = "SHUFFLE",
                displayName = "Shuffle Super Pack (x5)",
                quantity = 5,
                priceType = PriceType.COINS,
                priceAmount = 850L,
                bonusQuantity = 1,
                unlockLevel = 1
            ),
            ShopBundleConfig(
                bundleId = "EXTRA_MOVES_PACK_3",
                itemId = "EXTRA_MOVES",
                displayName = "+5 Moves Pack (x3)",
                quantity = 3,
                priceType = PriceType.GEMS,
                priceAmount = 250L,
                bonusQuantity = 0,
                unlockLevel = 1
            )
        )
    }
}
