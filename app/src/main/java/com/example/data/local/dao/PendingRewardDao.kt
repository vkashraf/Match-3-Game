package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PendingRewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingRewardDao {

    @Query("SELECT * FROM pending_rewards WHERE isClaimed = 0 ORDER BY createdAt ASC")
    fun observePendingRewards(): Flow<List<PendingRewardEntity>>

    @Query("SELECT * FROM pending_rewards WHERE isClaimed = 0 ORDER BY createdAt ASC")
    suspend fun getPendingRewards(): List<PendingRewardEntity>

    @Query("SELECT * FROM pending_rewards WHERE referenceId = :referenceId LIMIT 1")
    suspend fun getByReferenceId(referenceId: String): PendingRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingReward(reward: PendingRewardEntity)

    @Query("UPDATE pending_rewards SET isClaimed = 1 WHERE pendingRewardId = :id")
    suspend fun markClaimed(id: String)

    @Query("DELETE FROM pending_rewards WHERE isClaimed = 1")
    suspend fun clearClaimed()
}
