package com.bazyak.walletplus.wear.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WearPassDao {
    @Query("SELECT * FROM wear_passes ORDER BY displayOrder ASC, updatedAt DESC")
    fun observePasses(): Flow<List<WearPassEntity>>

    @Query("SELECT * FROM wear_passes WHERE id = :passId")
    fun observePass(passId: String): Flow<WearPassEntity?>

    @Query("SELECT * FROM wear_passes")
    suspend fun getAllPasses(): List<WearPassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pass: WearPassEntity)

    @Query("DELETE FROM wear_passes")
    suspend fun deleteAll()

    @Query("DELETE FROM wear_passes WHERE id NOT IN (:passIds)")
    suspend fun deleteMissing(passIds: List<String>)
}
