package com.bazyak.walletplus.data.model

/**
 * Represents the status of a pass refresh operation.
 */
sealed class RefreshStatus {
    /** No refresh operation in progress */
    data object Idle : RefreshStatus()

    /** Refresh operation in progress */
    data class Loading(val passId: String? = null) : RefreshStatus() // null = all passes

    /** Refresh completed successfully */
    data class Success(val updatedCount: Int) : RefreshStatus()

    /** Refresh failed with error */
    data class Error(val message: String, val passId: String? = null) : RefreshStatus()
}
