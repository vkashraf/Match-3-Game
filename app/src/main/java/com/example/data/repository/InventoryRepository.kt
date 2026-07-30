package com.example.data.repository

import com.example.data.local.dao.InventoryDao
import com.example.data.local.entity.InventoryItemEntity
import com.example.game.shop.config.EconomyConfig
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allItemsFlow: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()

    suspend fun getQuantity(itemId: String): Int {
        val item = inventoryDao.getItem(itemId)
        return item?.quantity ?: 0
    }

    suspend fun observeQuantity(itemId: String): Flow<InventoryItemEntity?> {
        return inventoryDao.observeItem(itemId)
    }

    suspend fun addItem(itemId: String, amount: Int, itemType: String = "BOOSTER"): Boolean {
        if (amount <= 0) return false
        val existing = inventoryDao.getItem(itemId)
        val currentQty = existing?.quantity ?: 0
        val newQty = (currentQty + amount).coerceAtMost(EconomyConfig.MAX_INVENTORY_QUANTITY)

        if (existing == null) {
            inventoryDao.insertItem(InventoryItemEntity(itemId = itemId, itemType = itemType, quantity = newQty))
        } else {
            inventoryDao.updateQuantity(itemId, newQty)
        }
        return true
    }

    suspend fun removeItem(itemId: String, amount: Int = 1): Boolean {
        if (amount <= 0) return false
        val existing = inventoryDao.getItem(itemId) ?: return false
        if (existing.quantity < amount) return false

        val newQty = existing.quantity - amount
        inventoryDao.updateQuantity(itemId, newQty)
        return true
    }

    suspend fun hasItem(itemId: String, amount: Int = 1): Boolean {
        return getQuantity(itemId) >= amount
    }
}
