package com.bazyak.walletplus.wear

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WearApiExceptionUtilsTest {
    @Test
    fun returnsTrueForWearableApiNotConnectedStatus() {
        val exception = ApiException(Status(CommonStatusCodes.API_NOT_CONNECTED))

        assertTrue(exception.isWearableApiUnavailable())
    }

    @Test
    fun returnsTrueForWearableApiUnavailableConnectionStatus() {
        val exception = ApiException(Status(ConnectionResult.API_UNAVAILABLE))

        assertTrue(exception.isWearableApiUnavailable())
    }

    @Test
    fun returnsTrueForWrappedWearableApiException() {
        val exception = IllegalStateException(
            ApiException(Status(CommonStatusCodes.API_NOT_CONNECTED)),
        )

        assertTrue(exception.isWearableApiUnavailable())
    }

    @Test
    fun returnsFalseForUnrelatedException() {
        val exception = ApiException(Status(CommonStatusCodes.NETWORK_ERROR))

        assertFalse(exception.isWearableApiUnavailable())
    }
}
