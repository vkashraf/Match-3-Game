package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WorldProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldProgressDao {

    @Query("SELECT * FROM world_progress WHERE worldId = :worldId LIMIT 1")
    suspend fun getWorld(worldId: Int): WorldProgressEntity?

    @Query("SELECT * FROM world_progress ORDER BY worldId ASC")
    fun getAllWorlds(): Flow<List<WorldProgressEntity>>

    @Query("SELECT COUNT(*) FROM world_progress")
    suspend fun getWorldCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorlds(worlds: List<WorldProgressEntity>)

    @Update
    suspend fun updateWorld(world: WorldProgressEntity)

    @Query("UPDATE world_progress SET isUnlocked = 1 WHERE worldId = :worldId")
    suspend fun unlockWorld(worldId: Int)

    @Query("UPDATE world_progress SET rewardClaimed = 1 WHERE worldId = :worldId")
    suspend fun claimReward(worldId: Int)
}
