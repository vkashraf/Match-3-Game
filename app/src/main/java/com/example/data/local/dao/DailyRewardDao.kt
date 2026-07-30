package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyRewardStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRewardDao {

    @Query("SELECT * FROM daily_reward_state WHERE id = 1")
    fun observeDailyRewardState(): Flow<DailyRewardStateEntity?>

    @Query("SELECT * FROM daily_reward_state WHERE id = 1")
    suspend fun getDailyRewardState(): DailyRewardStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateState(state: DailyRewardStateEntity)
}
