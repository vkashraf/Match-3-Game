package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MissionProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM mission_progress")
    fun observeAllMissions(): Flow<List<MissionProgressEntity>>

    @Query("SELECT * FROM mission_progress")
    suspend fun getAllMissions(): List<MissionProgressEntity>

    @Query("SELECT * FROM mission_progress WHERE missionId = :missionId")
    suspend fun getMission(missionId: String): MissionProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMission(mission: MissionProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMissions(missions: List<MissionProgressEntity>)

    @Query("DELETE FROM mission_progress WHERE isDaily = 1")
    suspend fun resetDailyMissions()
}
