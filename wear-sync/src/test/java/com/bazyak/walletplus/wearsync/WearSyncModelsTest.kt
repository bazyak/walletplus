package com.bazyak.walletplus.wearsync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WearSyncModelsTest {
    @Test
    fun snapshotRoundTripsThroughVersionedJson() {
        val snapshot = WearPassSnapshot(
            id = "pass-1",
            format = "PKPASS",
            title = "Coffee",
            subtitle = "Rewards",
            category = "STORE_CARD",
            backgroundColor = "#000000",
            foregroundColor = "#FFFFFF",
            labelColor = "#CCCCCC",
            displayOrder = 3,
            barcode = WearPassBarcode(
                value = "123456",
                format = WearBarcodeFormat.QR_CODE,
                altText = "Scan me",
            ),
            fields = listOf(
                WearPassField(
                    key = "points",
                    label = "Points",
                    value = "42",
                    section = WearPassFieldSection.BACK,
                ),
            ),
            assets = listOf(WearPassAssetRef("pass-1-icon", WearPassAssetType.ICON)),
        )

        val decoded = WearSyncJson.decodeSnapshot(WearSyncJson.encodeSnapshot(snapshot))

        assertEquals(WEAR_PASS_SCHEMA_VERSION, decoded.schemaVersion)
        assertEquals(snapshot, decoded)
        assertNotNull(decoded.barcode)
    }

    @Test
    fun passIdCanBeReadFromPassPath() {
        assertEquals("abc", WearSyncPaths.passIdFromPath("/wallet/pass/abc"))
        assertEquals(null, WearSyncPaths.passIdFromPath("/wallet/passes/index"))
    }

    @Test
    fun dotWalletOpenPassDeepLinkRoundTripsPassId() {
        val uri = DotWalletPassDeepLink.buildOpenPassUriString("pass id/with slash")

        assertEquals("dot-wallet://pass/pass%20id%2Fwith%20slash", uri)
        assertEquals("pass id/with slash", DotWalletPassDeepLink.passIdFromUriString(uri))
    }

    @Test
    fun dotWalletOpenPassDeepLinkRejectsOtherUris() {
        assertEquals(null, DotWalletPassDeepLink.passIdFromUriString("wallet://pass/abc"))
        assertEquals(null, DotWalletPassDeepLink.passIdFromUriString("dot-wallet://passes/abc"))
        assertEquals(null, DotWalletPassDeepLink.passIdFromUriString("dot-wallet://pass/"))
        assertEquals(null, DotWalletPassDeepLink.passIdFromUriString("dot-wallet://pass/%"))
    }
}
