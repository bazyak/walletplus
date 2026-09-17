package com.bazyak.walletplus.data.model

import kotlinx.serialization.Serializable

/**
 * JSON structure for custom passes created from scanned barcodes.
 * Stored in Pass.rawData field when format is PassFormat.CUSTOM.
 */
@Serializable
data class CustomPassJson(
    /** Schema version for future migrations */
    val version: Int = 1,

    /** Type of custom pass (currently only loyalty cards supported) */
    val type: String = "loyalty_card",

    /** Barcode value/data */
    val barcodeValue: String,

    /** Barcode format (e.g., "QR_CODE", "CODE_128", "PDF_417") */
    val barcodeFormat: String,

    /**
     * Icon name for Material Icons (e.g., "CreditCard", "Store", "LocalOffer").
     * Null for backward compatibility with existing cards.
     * Falls back to "CreditCard" icon if null or unrecognized.
     */
    val iconName: String? = null,
)
