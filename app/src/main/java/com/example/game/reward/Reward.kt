package com.example.game.reward

enum class RewardType {
    COINS,
    GEMS,
    ENERGY,
    BOOSTER,
    XP,
    WOOD,
    STONE,
    METAL,
    FOOD,
    MATERIAL
}

data class Reward(
    val type: RewardType,
    val itemId: String? = null,
    val quantity: Int = 0
) {
    fun getDisplayName(): String {
        return when (type) {
            RewardType.COINS -> "$quantity Coins"
            RewardType.GEMS -> "$quantity Gems"
            RewardType.ENERGY -> "$quantity Energy"
            RewardType.BOOSTER -> "$quantity ${itemId ?: "Booster"}"
            RewardType.XP -> "$quantity XP"
            RewardType.WOOD -> "$quantity Wood"
            RewardType.STONE -> "$quantity Stone"
            RewardType.METAL -> "$quantity Metal"
            RewardType.FOOD -> "$quantity Food"
            RewardType.MATERIAL -> "$quantity ${itemId ?: "Material"}"
        }
    }
}
