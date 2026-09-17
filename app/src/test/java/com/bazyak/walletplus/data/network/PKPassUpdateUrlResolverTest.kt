package com.bazyak.walletplus.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PKPassUpdateUrlResolverTest {
    @Test
    fun `query URL is treated as complete update endpoint`() {
        val webServiceUrl = "https://example.com/api/profile/wallet?user_id=123"

        val result = PKPassUpdateUrlResolver.resolve(
            webServiceUrl = webServiceUrl,
            passTypeIdentifier = "pass.example.loyalty",
            serialNumber = "serial-123",
        )

        assertEquals(webServiceUrl, result)
    }

    @Test
    fun `standard base URL without trailing slash uses Apple update path`() {
        val result = PKPassUpdateUrlResolver.resolve(
            webServiceUrl = "https://wallet.example.com/api",
            passTypeIdentifier = "pass.example.loyalty",
            serialNumber = "serial-123",
        )

        assertEquals(
            "https://wallet.example.com/api/v1/passes/pass.example.loyalty/serial-123",
            result,
        )
    }

    @Test
    fun `standard base URL with trailing slash uses Apple update path`() {
        val result = PKPassUpdateUrlResolver.resolve(
            webServiceUrl = "https://wallet.example.com/api/",
            passTypeIdentifier = "pass.example.loyalty",
            serialNumber = "serial-123",
        )

        assertEquals(
            "https://wallet.example.com/api/v1/passes/pass.example.loyalty/serial-123",
            result,
        )
    }

    @Test
    fun `path identifiers are encoded as individual segments`() {
        val result = PKPassUpdateUrlResolver.resolve(
            webServiceUrl = "https://wallet.example.com",
            passTypeIdentifier = "pass.example loyalty",
            serialNumber = "serial/123",
        )

        assertEquals(
            "https://wallet.example.com/v1/passes/pass.example%20loyalty/serial%2F123",
            result,
        )
    }

    @Test
    fun `invalid web service URL is rejected`() {
        val result = PKPassUpdateUrlResolver.resolve(
            webServiceUrl = "not a URL",
            passTypeIdentifier = "pass.example.loyalty",
            serialNumber = "serial-123",
        )

        assertNull(result)
    }
}
