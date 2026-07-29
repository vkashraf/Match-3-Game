package com.example.data.repository

import com.example.data.local.dao.BuildingPlotDao
import com.example.data.local.dao.IslandZoneDao
import com.example.data.local.entity.BuildingPlotEntity
import com.example.data.local.entity.IslandZoneEntity
import kotlinx.coroutines.flow.Flow

class IslandRepository(
    private val plotDao: BuildingPlotDao,
    private val zoneDao: IslandZoneDao
) {

    val allPlotsFlow: Flow<List<BuildingPlotEntity>> = plotDao.getAllPlots()
    val allZonesFlow: Flow<List<IslandZoneEntity>> = zoneDao.getAllZones()

    suspend fun getPlot(plotId: String): BuildingPlotEntity? {
        return plotDao.getPlot(plotId)
    }

    suspend fun updatePlot(plot: BuildingPlotEntity) {
        plotDao.updatePlot(plot)
    }

    suspend fun getZone(zoneId: Int): IslandZoneEntity? {
        return zoneDao.getZone(zoneId)
    }

    suspend fun updateZone(zone: IslandZoneEntity) {
        zoneDao.updateZone(zone)
    }
}
