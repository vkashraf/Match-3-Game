package com.example.game.shop.model

data class ShopItemConfig(
    val itemId: String,
    val itemType: String,
    val displayName: String,
    val description: String,
    val iconName: String,
    val priceType: PriceType,
    val priceAmount: Long,
    val quantity: Int = 1,
    val unlockLevel: Int = 1,
    val isAvailable: Boolean = true
)
