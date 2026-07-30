package com.example.game.shop.repository

import com.example.data.repository.InventoryRepository
import com.example.data.repository.PlayerRepository
import com.example.game.energy.EnergyManager
import com.example.game.shop.config.EconomyConfig
import com.example.game.shop.model.PriceType
import com.example.game.shop.model.PurchaseResult
import com.example.game.shop.model.ShopBundleConfig
import com.example.game.shop.model.ShopItemConfig
import java.util.concurrent.atomic.AtomicBoolean

class ShopRepository(
    private val playerRepository: PlayerRepository,
    private val inventoryRepository: InventoryRepository
) {

    private val isPurchasing = AtomicBoolean(false)

    suspend fun purchaseItem(item: ShopItemConfig): PurchaseResult {
        if (!isPurchasing.compareAndSet(false, true)) {
            return PurchaseResult(false, "Transaction in progress. Please wait.")
        }

        try {
            val player = playerRepository.getPlayer()

            // 1. Level Requirement Check
            if (player.playerLevel < item.unlockLevel) {
                return PurchaseResult(false, "Requires Player Level ${item.unlockLevel}.")
            }

            // 2. Special Energy Items Handling
            if (item.itemType == "ENERGY") {
                if (player.energy >= player.maxEnergy) {
                    return PurchaseResult(false, "Energy is already full!")
                }

                if (item.itemId == "ENERGY_1") {
                    val success = EnergyManager.refillSingleEnergyWithGems(playerRepository)
                    return if (success) PurchaseResult(true, "Refilled +1 Energy!") else PurchaseResult(false, "Not enough Gems!")
                } else if (item.itemId == "FULL_ENERGY") {
                    val success = EnergyManager.fullRefillEnergyWithGems(playerRepository)
                    return if (success) PurchaseResult(true, "Energy Fully Refilled!") else PurchaseResult(false, "Not enough Gems!")
                }
            }

            // 3. Inventory Quantity Limit Check
            val currentQty = inventoryRepository.getQuantity(item.itemId)
            if (currentQty + item.quantity > EconomyConfig.MAX_INVENTORY_QUANTITY) {
                return PurchaseResult(false, "Inventory limit reached (${EconomyConfig.MAX_INVENTORY_QUANTITY} max).")
            }

            // 4. Currency Sufficiency Check
            when (item.priceType) {
                PriceType.COINS -> {
                    if (player.coins < item.priceAmount) {
                        return PurchaseResult(false, "Not enough Coins! Need ${item.priceAmount} Coins.")
                    }
                }
                PriceType.GEMS -> {
                    if (player.gems < item.priceAmount) {
                        return PurchaseResult(false, "Not enough Gems! Need ${item.priceAmount} Gems.")
                    }
                }
                PriceType.FREE -> { /* No cost */ }
                PriceType.ENERGY -> {
                    if (player.energy < item.priceAmount) {
                        return PurchaseResult(false, "Not enough Energy!")
                    }
                }
            }

            // 5. Deduct Currency Atomically
            val deducted = when (item.priceType) {
                PriceType.COINS -> playerRepository.spendCoins(item.priceAmount)
                PriceType.GEMS -> playerRepository.spendGems(item.priceAmount.toInt())
                PriceType.ENERGY -> playerRepository.spendEnergy(item.priceAmount.toInt())
                PriceType.FREE -> true
            }

            if (!deducted) {
                return PurchaseResult(false, "Failed to complete currency transaction.")
            }

            // 6. Grant Material or Inventory Item
            if (item.itemType == "MATERIAL") {
                val resType = when {
                    item.itemId.startsWith("WOOD") -> com.example.game.resource.ResourceType.WOOD
                    item.itemId.startsWith("STONE") -> com.example.game.resource.ResourceType.STONE
                    item.itemId.startsWith("METAL") -> com.example.game.resource.ResourceType.METAL
                    item.itemId.startsWith("FOOD") -> com.example.game.resource.ResourceType.FOOD
                    else -> com.example.game.resource.ResourceType.WOOD
                }
                com.example.game.resource.ResourceManager.addResource(resType, item.quantity, ignoreCapacity = false)
            } else {
                inventoryRepository.addItem(item.itemId, item.quantity, item.itemType)
            }

            com.example.core.event.GameEventBus.emit(
                com.example.core.event.GameEvent(
                    com.example.core.event.GameEventType.ITEM_PURCHASED,
                    amount = item.quantity,
                    itemId = item.itemId
                )
            )

            return PurchaseResult(true, "Successfully purchased ${item.displayName}!")
        } finally {
            isPurchasing.set(false)
        }
    }

    suspend fun purchaseBundle(bundle: ShopBundleConfig): PurchaseResult {
        if (!isPurchasing.compareAndSet(false, true)) {
            return PurchaseResult(false, "Transaction in progress. Please wait.")
        }

        try {
            val player = playerRepository.getPlayer()

            if (player.playerLevel < bundle.unlockLevel) {
                return PurchaseResult(false, "Requires Player Level ${bundle.unlockLevel}.")
            }

            val totalQuantity = bundle.quantity + bundle.bonusQuantity
            val currentQty = inventoryRepository.getQuantity(bundle.itemId)
            if (currentQty + totalQuantity > EconomyConfig.MAX_INVENTORY_QUANTITY) {
                return PurchaseResult(false, "Inventory limit reached (${EconomyConfig.MAX_INVENTORY_QUANTITY} max).")
            }

            val deducted = when (bundle.priceType) {
                PriceType.COINS -> playerRepository.spendCoins(bundle.priceAmount)
                PriceType.GEMS -> playerRepository.spendGems(bundle.priceAmount.toInt())
                PriceType.FREE -> true
                PriceType.ENERGY -> playerRepository.spendEnergy(bundle.priceAmount.toInt())
            }

            if (!deducted) {
                val currencyName = if (bundle.priceType == PriceType.COINS) "Coins" else "Gems"
                return PurchaseResult(false, "Not enough $currencyName!")
            }

            inventoryRepository.addItem(bundle.itemId, totalQuantity, "BOOSTER")
            return PurchaseResult(true, "Purchased ${bundle.displayName}!")
        } finally {
            isPurchasing.set(false)
        }
    }
}
