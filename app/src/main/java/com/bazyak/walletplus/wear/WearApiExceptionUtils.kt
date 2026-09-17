package com.bazyak.walletplus.wear

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

internal fun Throwable.isWearableApiUnavailable(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is ApiException && current.statusCode in wearableApiUnavailableStatusCodes) {
            return true
        }
        current = current.cause
    }
    return false
}

private val wearableApiUnavailableStatusCodes = setOf(
    CommonStatusCodes.API_NOT_CONNECTED,
    ConnectionResult.API_UNAVAILABLE,
)
