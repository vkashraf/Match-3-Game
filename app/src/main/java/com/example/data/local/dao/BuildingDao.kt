package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {

    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE buildingId = :buildingId")
    suspend fun getBuilding(buildingId: String): BuildingEntity?

    @Query("SELECT COUNT(*) FROM buildings")
    suspend fun getBuildingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildings(buildings: List<BuildingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    @Update
    suspend fun updateBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings WHERE buildingId = :buildingId")
    suspend fun deleteBuilding(buildingId: String)
}
