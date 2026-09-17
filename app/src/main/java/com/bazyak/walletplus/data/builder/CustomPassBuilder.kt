package com.bazyak.walletplus.data.builder

import com.bazyak.walletplus.data.json.WalletJson
import com.bazyak.walletplus.data.model.CustomPassJson
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassCategory
import com.bazyak.walletplus.data.model.PassFormat
import java.util.UUID
import kotlinx.serialization.encodeToString

/**
 * Builder for creating custom passes from scanned barcodes.
 */
object CustomPassBuilder {
    /**
     * Creates a custom pass from barcode scan data.
     *
     * @param cardName User-provided name for the card
     * @param barcodeValue The scanned barcode value
     * @param barcodeFormat The barcode format (e.g., "QR_CODE", "CODE_128")
     * @param iconName Material Icon name (e.g., "CreditCard", "Store")
     * @param backgroundColor Background color in hex format (e.g., "#2196F3")
     * @param foregroundColor Foreground (text) color in hex format (e.g., "#FFFFFF")
     * @return A Pass entity ready for database insertion
     */
    fun createCustomPass(
        cardName: String,
        barcodeValue: String,
        barcodeFormat: String,
        iconName: String,
        backgroundColor: String,
        foregroundColor: String = "#FFFFFF",
        labelColor: String = "#E0E0E0",
    ): Pass {
        val passId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val customPassJson = CustomPassJson(
            version = 1,
            type = "loyalty_card",
            barcodeValue = barcodeValue,
            barcodeFormat = barcodeFormat,
            iconName = iconName,
        )

        return Pass(
            id = passId,
            format = PassFormat.CUSTOM,
            organizationName = cardName,
            description = cardName,
            iconPath = "", // Custom cards don't have icons
            logoPath = null,
            stripPath = null,
            backgroundPath = null,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            labelColor = labelColor,
            assetsDirectory = "", // No assets for custom cards
            rawData = WalletJson.json.encodeToString(customPassJson),
            importedDate = currentTime,
            lastRefreshDate = null,
            autoRefreshEnabled = false, // Custom cards cannot refresh
            displayOrder = 0,
            category = PassCategory.STORE_CARD,
        )
    }
}
