package com.bazyak.walletplus.data.model

import com.google.zxing.BarcodeFormat

/**
 * Enum representing supported barcode formats.
 */
enum class BarcodeFormatType(val zxingFormat: BarcodeFormat) {
    QR_CODE(BarcodeFormat.QR_CODE),
    CODE_128(BarcodeFormat.CODE_128),
    CODE_39(BarcodeFormat.CODE_39),
    CODE_93(BarcodeFormat.CODE_93),
    CODABAR(BarcodeFormat.CODABAR),
    DATA_MATRIX(BarcodeFormat.DATA_MATRIX),
    EAN_13(BarcodeFormat.EAN_13),
    EAN_8(BarcodeFormat.EAN_8),
    ITF(BarcodeFormat.ITF),
    UPC_A(BarcodeFormat.UPC_A),
    UPC_E(BarcodeFormat.UPC_E),
    PDF_417(BarcodeFormat.PDF_417),
    AZTEC(BarcodeFormat.AZTEC),
    ;

    val isQRCode: Boolean
        get() = this == QR_CODE

    companion object {
        /**
         * Convert format name string to BarcodeFormatType.
         */
        fun fromName(name: String): BarcodeFormatType = when (name) {
            "CODE_128" -> CODE_128
            "CODE_39" -> CODE_39
            "CODE_93" -> CODE_93
            "CODABAR" -> CODABAR
            "DATA_MATRIX" -> DATA_MATRIX
            "EAN_13" -> EAN_13
            "EAN_8" -> EAN_8
            "ITF" -> ITF
            "QR_CODE" -> QR_CODE
            "UPC_A" -> UPC_A
            "UPC_E" -> UPC_E
            "PDF417" -> PDF_417
            "AZTEC" -> AZTEC
            else -> QR_CODE // Default to QR code
        }

        /**
         * Convert PKPass format string to BarcodeFormatType.
         */
        fun fromPKPassFormat(pkFormat: String): BarcodeFormatType? = when (pkFormat) {
            "PKBarcodeFormatQR" -> QR_CODE
            "PKBarcodeFormatPDF417" -> PDF_417
            "PKBarcodeFormatAztec" -> AZTEC
            "PKBarcodeFormatCode128" -> CODE_128
            else -> null
        }
    }
}
