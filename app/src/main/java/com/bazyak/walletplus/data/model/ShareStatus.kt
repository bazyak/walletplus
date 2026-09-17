package com.bazyak.walletplus.data.model

import com.bazyak.walletplus.data.exporter.ExportResult

/**
 * Represents the status of a pass share operation.
 */
sealed class ShareStatus {
    object Idle : ShareStatus()
    data class Loading(val passId: String) : ShareStatus()
    data class Success(val exportResult: ExportResult, val passId: String) : ShareStatus()
    data class Error(val message: String, val passId: String) : ShareStatus()
}
