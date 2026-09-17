package com.bazyak.walletplus.wear.ui

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.bazyak.walletplus.wearsync.WearBarcodeFormat

const val WEAR_BARCODE_ROTATION_ENABLED = false

fun generateWearBarcodeBitmap(
    value: String,
    format: WearBarcodeFormat,
    rotateForWear: Boolean = WEAR_BARCODE_ROTATION_ENABLED,
): Bitmap? = runCatching {
    val source = generateBitmap(
        value = value,
        format = format.toZxingFormat(),
        width = if (format.isSquareCode()) 1000 else 1400,
        height = if (format.isSquareCode()) 1000 else 520,
    )

    source
}.getOrNull()

private fun generateBitmap(value: String, format: BarcodeFormat, width: Int, height: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(value, format, width, height)
    val pixels = IntArray(bitMatrix.width * bitMatrix.height)

    for (y in 0 until bitMatrix.height) {
        val offset = y * bitMatrix.width
        for (x in 0 until bitMatrix.width) {
            pixels[offset + x] = if (bitMatrix[x, y]) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }

    return Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, bitMatrix.width, 0, 0, bitMatrix.width, bitMatrix.height)
    }
}

private fun WearBarcodeFormat.toZxingFormat(): BarcodeFormat = when (this) {
    WearBarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
    WearBarcodeFormat.CODE_128 -> BarcodeFormat.CODE_128
    WearBarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
    WearBarcodeFormat.CODE_93 -> BarcodeFormat.CODE_93
    WearBarcodeFormat.CODABAR -> BarcodeFormat.CODABAR
    WearBarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
    WearBarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
    WearBarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
    WearBarcodeFormat.ITF -> BarcodeFormat.ITF
    WearBarcodeFormat.UPC_A -> BarcodeFormat.UPC_A
    WearBarcodeFormat.UPC_E -> BarcodeFormat.UPC_E
    WearBarcodeFormat.PDF_417 -> BarcodeFormat.PDF_417
    WearBarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
}

private fun WearBarcodeFormat.isSquareCode(): Boolean = when (this) {
    WearBarcodeFormat.QR_CODE,
    WearBarcodeFormat.DATA_MATRIX,
    WearBarcodeFormat.AZTEC,
    -> true
    WearBarcodeFormat.CODE_128,
    WearBarcodeFormat.CODE_39,
    WearBarcodeFormat.CODE_93,
    WearBarcodeFormat.CODABAR,
    WearBarcodeFormat.EAN_13,
    WearBarcodeFormat.EAN_8,
    WearBarcodeFormat.ITF,
    WearBarcodeFormat.UPC_A,
    WearBarcodeFormat.UPC_E,
    WearBarcodeFormat.PDF_417,
    -> false
}
