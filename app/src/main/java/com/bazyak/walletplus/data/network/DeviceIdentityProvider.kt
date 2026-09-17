package com.bazyak.walletplus.data.network

import android.content.Context
import java.security.SecureRandom

/**
 * Supplies the identifiers the PassKit web service protocol expects from a device.
 *
 * Both values are generated once and kept for the lifetime of the install. The identifier must be
 * stable, because it is the key an issuer stores registrations under: regenerating it would orphan
 * every previous registration and re-register the same passes as new devices.
 *
 * The push token is a stand-in. The protocol was designed around APNs, which Android has no access
 * to, so no notification will ever arrive at it — but issuers require the field to be present and
 * store it verbatim, and registration is what makes many of them serve pass bodies at all.
 */
internal class DeviceIdentityProvider(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val deviceLibraryIdentifier: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: generateAndStore(KEY_DEVICE_ID, DEVICE_ID_BYTES)

    val pushToken: String
        get() = prefs.getString(KEY_PUSH_TOKEN, null) ?: generateAndStore(KEY_PUSH_TOKEN, PUSH_TOKEN_BYTES)

    private fun generateAndStore(key: String, byteCount: Int): String {
        val bytes = ByteArray(byteCount).also { SecureRandom().nextBytes(it) }
        val value = bytes.joinToString("") { "%02x".format(it) }

        prefs.edit().putString(key, value).apply()
        return value
    }

    private companion object {
        const val PREFS_NAME = "pkpass_device_identity"
        const val KEY_DEVICE_ID = "device_library_identifier"
        const val KEY_PUSH_TOKEN = "push_token"

        // Sized to match what Apple's own clients send, since some issuers validate the length.
        const val DEVICE_ID_BYTES = 16
        const val PUSH_TOKEN_BYTES = 32
    }
}
