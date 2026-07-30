package com.example.game.shop.model

data class ShopBundleConfig(
    val bundleId: String,
    val itemId: String,
    val displayName: String,
    val quantity: Int,
    val priceType: PriceType,
    val priceAmount: Long,
    val bonusQuantity: Int = 0,
    val unlockLevel: Int = 1
)
