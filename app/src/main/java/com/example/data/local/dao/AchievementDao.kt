package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AchievementProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievement_progress")
    fun observeAllAchievements(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT * FROM achievement_progress")
    suspend fun getAllAchievements(): List<AchievementProgressEntity>

    @Query("SELECT * FROM achievement_progress WHERE achievementId = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: AchievementProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievements(achievements: List<AchievementProgressEntity>)
}
