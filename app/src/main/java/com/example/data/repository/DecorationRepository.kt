package com.example.data.repository

import com.example.data.local.dao.DecorationDao
import com.example.data.local.entity.DecorationEntity
import kotlinx.coroutines.flow.Flow

class DecorationRepository(private val decorationDao: DecorationDao) {

    val allDecorationsFlow: Flow<List<DecorationEntity>> = decorationDao.getAllDecorations()

    suspend fun getAllDecorations(): List<DecorationEntity> {
        return decorationDao.getAllDecorationsList()
    }

    suspend fun getDecoration(id: String): DecorationEntity? {
        return decorationDao.getDecoration(id)
    }

    suspend fun insertDecoration(decoration: DecorationEntity) {
        decorationDao.insertDecoration(decoration)
    }

    suspend fun updateDecoration(decoration: DecorationEntity) {
        decorationDao.updateDecoration(decoration)
    }

    suspend fun deleteDecoration(id: String) {
        decorationDao.deleteDecoration(id)
    }
}
