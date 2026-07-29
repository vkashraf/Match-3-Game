package com.example.data.repository

import com.example.data.local.dao.InventoryDao
import com.example.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allItemsFlow: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()

    suspend fun addItem(item: InventoryItemEntity) {
        inventoryDao.insertItem(item)
    }

    suspend fun updateItem(item: InventoryItemEntity) {
        inventoryDao.updateItem(item)
    }
}
