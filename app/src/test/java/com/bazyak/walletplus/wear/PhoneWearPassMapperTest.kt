package com.bazyak.walletplus.wear

import com.bazyak.walletplus.data.model.CustomPassJson
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassCategory
import com.bazyak.walletplus.data.model.PassFormat
import com.bazyak.walletplus.data.parser.pkpass.PKBarcode
import com.bazyak.walletplus.data.parser.pkpass.PKField
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.data.parser.pkpass.PKPassStructure
import com.bazyak.walletplus.wearsync.WearBarcodeFormat
import com.bazyak.walletplus.wearsync.WearPassFieldSection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PhoneWearPassMapperTest {
    @Test
    fun mapsPkPassBarcodeAndFieldsToWearSnapshot() {
        val rawData = Json.encodeToString(
            PKPassJson(
                formatVersion = 1,
                passTypeIdentifier = "pass.test",
                serialNumber = "serial",
                organizationName = "Cafe",
                description = "Rewards",
                storeCard = PKPassStructure(
                    headerFields = listOf(PKField("tier", "Tier", "Gold")),
                    primaryFields = listOf(
                        PKField("points", "Points", "120"),
                        PKField("empty", "Empty", ""),
                    ),
                    secondaryFields = listOf(PKField("member", "Member", "Ana")),
                    auxiliaryFields = listOf(PKField("expires", "Expires", "Tomorrow")),
                    backFields = listOf(PKField("terms", "Terms", "Valid today")),
                ),
                barcodes = listOf(PKBarcode("PKBarcodeFormatPDF417", "ABC", "iso-8859-1", "ABC")),
            ),
        )

        val snapshot = pass(format = PassFormat.PKPASS, rawData = rawData).toWearPassSnapshot(formatLabel = "Format")

        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertEquals(WearBarcodeFormat.PDF_417, snapshot.barcode?.format)
        assertEquals(5, snapshot.fields.size)
        assertEquals(WearPassFieldSection.HEADER, snapshot.fields[0].section)
        assertEquals(WearPassFieldSection.PRIMARY, snapshot.fields.first { it.key == "points" }.section)
        assertEquals(false, snapshot.fields.any { it.key == "empty" })
        assertEquals(WearPassFieldSection.SECONDARY, snapshot.fields.first { it.key == "member" }.section)
        assertEquals(WearPassFieldSection.AUXILIARY, snapshot.fields.first { it.key == "expires" }.section)
    }

    @Test
    fun mapsCustomPassToWearSnapshot() {
        val rawData = Json.encodeToString(
            CustomPassJson(
                barcodeValue = "123",
                barcodeFormat = "QR_CODE",
                iconName = "Store",
            ),
        )

        val snapshot = pass(format = PassFormat.CUSTOM, rawData = rawData).toWearPassSnapshot(formatLabel = "Format")

        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertEquals("Cafe", snapshot.title)
        assertEquals(WearBarcodeFormat.QR_CODE, snapshot.barcode?.format)
        assertEquals("Store", snapshot.customIconName)
    }

    private fun pass(format: PassFormat, rawData: String) = Pass(
        id = "id",
        format = format,
        organizationName = "Cafe",
        description = "Rewards",
        iconPath = "",
        logoPath = null,
        stripPath = null,
        backgroundPath = null,
        foregroundColor = "#FFFFFF",
        backgroundColor = "#000000",
        labelColor = "#EEEEEE",
        assetsDirectory = "",
        rawData = rawData,
        importedDate = 1,
        displayOrder = 2,
        category = PassCategory.STORE_CARD,
    )
}
