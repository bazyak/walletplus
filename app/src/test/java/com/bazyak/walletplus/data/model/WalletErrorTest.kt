package com.bazyak.walletplus.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WalletErrorTest {
    @Test
    fun `walletErrorOr returns typed wallet error when throwable wraps one`() {
        val throwable = WalletError.NoExporterAvailable("PKPASS").asException()

        assertEquals(WalletError.NoExporterAvailable("PKPASS"), throwable.walletErrorOr(WalletError.Unknown))
    }

    @Test
    fun `walletErrorOr returns default for unknown throwable`() {
        val throwable = IllegalStateException("third-party failure")

        assertEquals(WalletError.Unknown, throwable.walletErrorOr(WalletError.Unknown))
    }
}
