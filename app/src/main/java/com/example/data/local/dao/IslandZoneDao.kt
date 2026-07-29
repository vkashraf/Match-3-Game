package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.IslandZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IslandZoneDao {

    @Query("SELECT * FROM island_zones")
    fun getAllZones(): Flow<List<IslandZoneEntity>>

    @Query("SELECT * FROM island_zones WHERE zoneId = :zoneId")
    suspend fun getZone(zoneId: Int): IslandZoneEntity?

    @Query("SELECT COUNT(*) FROM island_zones")
    suspend fun getZoneCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<IslandZoneEntity>)

    @Update
    suspend fun updateZone(zone: IslandZoneEntity)
}
