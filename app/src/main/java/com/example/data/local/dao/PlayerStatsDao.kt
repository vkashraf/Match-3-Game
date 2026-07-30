package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {

    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun observePlayerStats(): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getPlayerStats(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: PlayerStatsEntity)
}
