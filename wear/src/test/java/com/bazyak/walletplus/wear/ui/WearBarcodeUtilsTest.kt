package com.bazyak.walletplus.wear.ui

import org.junit.Assert.assertFalse
import org.junit.Test

class WearBarcodeUtilsTest {
    @Test
    fun squareCodesStayUpright() {
        assertFalse(WEAR_BARCODE_ROTATION_ENABLED)
    }

    @Test
    fun wideCodesStayUpright() {
        assertFalse(WEAR_BARCODE_ROTATION_ENABLED)
    }
}
