package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DecorationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DecorationDao {

    @Query("SELECT * FROM decorations")
    fun getAllDecorations(): Flow<List<DecorationEntity>>

    @Query("SELECT * FROM decorations")
    suspend fun getAllDecorationsList(): List<DecorationEntity>

    @Query("SELECT * FROM decorations WHERE decorationInstanceId = :id")
    suspend fun getDecoration(id: String): DecorationEntity?

    @Query("SELECT COUNT(*) FROM decorations")
    suspend fun getDecorationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecoration(decoration: DecorationEntity)

    @Update
    suspend fun updateDecoration(decoration: DecorationEntity)

    @Query("DELETE FROM decorations WHERE decorationInstanceId = :id")
    suspend fun deleteDecoration(id: String)
}
