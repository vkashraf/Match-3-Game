package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItem(itemId: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId LIMIT 1")
    fun observeItem(itemId: String): Flow<InventoryItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantity = :quantity WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: String, quantity: Int)

    @Query("DELETE FROM inventory_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)
}
