package com.bazyak.walletplus.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bazyak.walletplus.data.model.Pass
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pass entities.
 * Provides format-agnostic database operations.
 */
@Dao
interface PassDao {
    /**
     * Get all passes as a Flow for reactive updates.
     * Ordered by display order (custom sort), then imported date as fallback.
     */
    @Query("SELECT * FROM passes ORDER BY displayOrder ASC, importedDate DESC")
    fun getAllPasses(): Flow<List<Pass>>

    /**
     * Get all passes as a list (non-reactive).
     * Ordered by display order (custom sort), then imported date as fallback.
     */
    @Query("SELECT * FROM passes ORDER BY displayOrder ASC, importedDate DESC")
    suspend fun getAllPassesList(): List<Pass>

    /**
     * Get a specific pass by ID.
     */
    @Query("SELECT * FROM passes WHERE id = :passId")
    suspend fun getPassById(passId: String): Pass?

    /**
     * Insert a new pass. Replaces if already exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: Pass)

    /**
     * Update an existing pass.
     */
    @Update
    suspend fun updatePass(pass: Pass)

    /**
     * Delete a pass.
     */
    @Delete
    suspend fun deletePass(pass: Pass)

    /**
     * Delete a pass by ID.
     */
    @Query("DELETE FROM passes WHERE id = :passId")
    suspend fun deletePassById(passId: String)

    /**
     * Update the display order of a specific pass.
     */
    @Query("UPDATE passes SET displayOrder = :order WHERE id = :passId")
    suspend fun updateDisplayOrder(passId: String, order: Int)

    /**
     * Update display orders for multiple passes in a transaction.
     */
    @Transaction
    suspend fun updateDisplayOrders(updates: List<Pair<String, Int>>) {
        updates.forEach { (passId, order) ->
            updateDisplayOrder(passId, order)
        }
    }

    /**
     * Get the maximum display order value (for assigning new passes to end).
     */
    @Query("SELECT MAX(displayOrder) FROM passes")
    suspend fun getMaxDisplayOrder(): Int?

    /**
     * Update the autoRefreshEnabled setting for a specific pass.
     */
    @Query("UPDATE passes SET autoRefreshEnabled = :enabled WHERE id = :passId")
    suspend fun updateAutoRefreshEnabled(passId: String, enabled: Boolean)
}
