package com.example.data.repository

import com.example.data.local.dao.BuildingDao
import com.example.data.local.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

class BuildingRepository(private val buildingDao: BuildingDao) {

    val allBuildingsFlow: Flow<List<BuildingEntity>> = buildingDao.getAllBuildings()

    suspend fun getBuilding(buildingId: String): BuildingEntity? {
        return buildingDao.getBuilding(buildingId)
    }

    suspend fun insertBuilding(building: BuildingEntity) {
        buildingDao.insertBuilding(building)
    }

    suspend fun updateBuilding(building: BuildingEntity) {
        buildingDao.updateBuilding(building)
    }

    suspend fun deleteBuilding(buildingId: String) {
        buildingDao.deleteBuilding(buildingId)
    }
}
