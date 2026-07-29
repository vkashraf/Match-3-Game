package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BuildingPlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingPlotDao {

    @Query("SELECT * FROM building_plots")
    fun getAllPlots(): Flow<List<BuildingPlotEntity>>

    @Query("SELECT * FROM building_plots WHERE plotId = :plotId")
    suspend fun getPlot(plotId: String): BuildingPlotEntity?

    @Query("SELECT COUNT(*) FROM building_plots")
    suspend fun getPlotCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<BuildingPlotEntity>)

    @Update
    suspend fun updatePlot(plot: BuildingPlotEntity)
}
