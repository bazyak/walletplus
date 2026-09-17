package com.bazyak.walletplus.ui.viewmodel

sealed class ImportStatus {
    data object Idle : ImportStatus()
    data object Loading : ImportStatus()
    data object Success : ImportStatus()
    data class Summary(val message: String) : ImportStatus()
    data class Error(val message: String) : ImportStatus()
}

sealed class PreviewStatus {
    data object Idle : PreviewStatus()
    data object Loading : PreviewStatus()
    data object Ready : PreviewStatus()
    data class Error(val message: String) : PreviewStatus()
}
