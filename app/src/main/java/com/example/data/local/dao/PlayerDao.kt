package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player WHERE playerId = 1 LIMIT 1")
    suspend fun getPlayer(): PlayerEntity?

    @Query("SELECT * FROM player WHERE playerId = 1 LIMIT 1")
    fun observePlayer(): Flow<PlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("UPDATE player SET coins = :newCoins, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateCoins(newCoins: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE player SET gems = :newGems, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateGems(newGems: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE player SET energy = :newEnergy, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateEnergy(newEnergy: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE player SET xp = :newXp, playerLevel = :newLevel, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateXpAndLevel(newXp: Long, newLevel: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE player SET totalStars = :newStars, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateStars(newStars: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE player SET currentLevel = :newCurrentLevel, lastPlayedAt = :now WHERE playerId = 1")
    suspend fun updateCurrentLevel(newCurrentLevel: Int, now: Long = System.currentTimeMillis())
}
