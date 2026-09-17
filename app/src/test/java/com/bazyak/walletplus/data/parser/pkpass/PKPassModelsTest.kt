package com.bazyak.walletplus.data.parser.pkpass

import com.bazyak.walletplus.data.json.WalletJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PKPassModelsTest {

    @Test
    fun `barcode without messageEncoding falls back to the default encoding`() {
        val json = """
            {"format":"PKBarcodeFormatCode128","message":"6666000116612674","altText":"2222 0001"}
        """.trimIndent()

        val barcode = WalletJson.json.decodeFromString<PKBarcode>(json)

        assertEquals("PKBarcodeFormatCode128", barcode.format)
        assertEquals("6666000116612674", barcode.message)
        assertEquals(PKBarcode.DEFAULT_MESSAGE_ENCODING, barcode.messageEncoding)
    }

    @Test
    fun `explicit messageEncoding is preserved`() {
        val json = """
            {"format":"PKBarcodeFormatQR","message":"abc","messageEncoding":"utf-8"}
        """.trimIndent()

        assertEquals("utf-8", WalletJson.json.decodeFromString<PKBarcode>(json).messageEncoding)
    }

    @Test
    fun `store card pass without messageEncoding is deserialized`() {
        val json = """
            {
              "formatVersion": 1,
              "passTypeIdentifier": "pass.ru.av.vkusomania",
              "serialNumber": "2222000116458934",
              "teamIdentifier": "Q87Y48W24E",
              "organizationName": "Азбука Вкуса",
              "description": "Клубная карта",
              "webServiceURL": "https://services-api.av.ru",
              "authenticationToken": "token",
              "sharingProhibited": true,
              "beacons": [],
              "locations": [],
              "barcodes": [
                {
                  "format": "PKBarcodeFormatCode128",
                  "altText": "2222 0001 1645 8934",
                  "message": "6666000116612674"
                }
              ],
              "storeCard": {
                "headerFields": [{"key": "bonuses", "label": "Бонусы:", "value": "10 935"}],
                "backFields": [{"key": "owner", "label": "Владелец карты", "value": "Руслан"}]
              }
            }
        """.trimIndent()

        val pass = WalletJson.json.decodeFromString<PKPassJson>(json)

        assertNotNull(pass.storeCard)
        assertEquals(1, pass.barcodes?.size)
        assertEquals(PKBarcode.DEFAULT_MESSAGE_ENCODING, pass.barcodes?.first()?.messageEncoding)
        assertEquals("10 935", pass.storeCard?.headerFields?.first()?.value)
    }
}
