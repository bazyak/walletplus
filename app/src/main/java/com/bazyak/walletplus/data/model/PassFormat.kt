package com.bazyak.walletplus.data.model

/**
 * Supported pass formats in the wallet application.
 * This enum enables multimodal support for different pass types.
 */
enum class PassFormat {
    /** Apple Wallet PKPass format (.pkpass files) */
    PKPASS,

    /** Google Wallet format (future support) */
    GOOGLE_WALLET,

    /** Custom pass format (future support) */
    CUSTOM,
}
