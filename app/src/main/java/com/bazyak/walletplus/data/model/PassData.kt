package com.bazyak.walletplus.data.model

import com.bazyak.walletplus.data.json.WalletJson
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson

/**
 * Sealed interface for type-safe pass data access.
 * Each pass format has its own subtype with format-specific data.
 *
 * This architecture provides:
 * - Compile-time safety: exhaustive when expressions
 * - Easy extensibility: add new subtypes for new formats
 * - Type-specific properties and methods per format
 */
sealed interface PassData {
    /**
     * PKPass (Apple Wallet) pass data
     */
    data class PKPass(val pkPassJson: PKPassJson) : PassData

    /**
     * Custom pass data (created from scanned barcodes)
     */
    data class Custom(val customPassJson: CustomPassJson) : PassData

    // Future formats:
    // data class GoogleWallet(val googleWalletJson: GoogleWalletJson) : PassData
}

/**
 * Extension function to parse rawData JSON into typed PassData.
 * Uses the format field to determine which subtype to deserialize.
 */
fun Pass.getPassData(): PassData = when (format) {
    PassFormat.PKPASS -> PassData.PKPass(
        WalletJson.json.decodeFromString<PKPassJson>(rawData),
    )
    PassFormat.CUSTOM -> PassData.Custom(
        WalletJson.json.decodeFromString<CustomPassJson>(rawData),
    )
    PassFormat.GOOGLE_WALLET -> throw UnsupportedOperationException(
        "Google Wallet format not yet implemented",
    )
}
