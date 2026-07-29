package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevel(levelId: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun observeLevel(levelId: Int): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevels(): Flow<List<LevelProgressEntity>>

    @Query("SELECT COUNT(*) FROM level_progress")
    suspend fun getLevelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevels(levels: List<LevelProgressEntity>)

    @Update
    suspend fun updateLevel(level: LevelProgressEntity)

    @Query("UPDATE level_progress SET isUnlocked = 1 WHERE levelId = :levelId")
    suspend fun unlockLevel(levelId: Int)
}
