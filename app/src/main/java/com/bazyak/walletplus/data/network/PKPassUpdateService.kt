package com.bazyak.walletplus.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bazyak.walletplus.data.json.WalletJson
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.data.parser.pkpass.PKPassParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit

/**
 * Service for updating PKPass cards via Apple Wallet update protocol.
 * Handles update URL resolution and API communication.
 */
class PKPassUpdateService(private val context: Context, private val pkPassParser: PKPassParser) {

    private val apiService: PKPassApiService = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(NetworkModule.okHttpClient)
        .build()
        .create(PKPassApiService::class.java)

    private val deviceIdentity = DeviceIdentityProvider(context)

    /**
     * Result of a pass update operation.
     */
    sealed class UpdateResult {
        /** Pass was updated successfully with new data */
        data class Updated(val newPassJson: PKPassJson) : UpdateResult()

        /** Pass has not been modified (304) */
        data object NotModified : UpdateResult()

        /** Pass was deleted/voided on server (404) */
        data object Deleted : UpdateResult()

        /** Authentication failed (401) */
        data object Unauthorized : UpdateResult()

        /** Network error occurred */
        data class NetworkError(val error: WalletError) : UpdateResult()

        /** Pass does not have web service configured */
        data object NoWebService : UpdateResult()
    }

    /**
     * Attempts to update a pass by fetching new data from its web service.
     *
     * @param pass The pass to update
     * @return UpdateResult indicating the outcome
     */
    suspend fun updatePass(pass: Pass): UpdateResult {
        return try {
            // Parse rawData JSON to extract web service info
            val passJson = WalletJson.json.decodeFromString<PKPassJson>(pass.rawData)

            // Check if pass has web service configured
            val webServiceURL = passJson.webServiceURL
            val authToken = passJson.authenticationToken
            val passTypeIdentifier = passJson.passTypeIdentifier
            val serialNumber = passJson.serialNumber

            if (webServiceURL == null || authToken == null) {
                return UpdateResult.NoWebService
            }

            val updateUrl = PKPassUpdateUrlResolver.resolve(
                webServiceUrl = webServiceURL,
                passTypeIdentifier = passTypeIdentifier,
                serialNumber = serialNumber,
            ) ?: throw IllegalArgumentException("Invalid PKPass web service URL")

            // Call API with Authorization header
            val authHeader = "ApplePass $authToken"
            val response = apiService.getPassUpdate(
                updateUrl = updateUrl,
                authToken = authHeader,
            )

            // Dump every response before interpreting it. Issuers misuse status codes (a 200 with
            // an empty body, an HTML interstitial, a non-standard 4xx), so the raw bytes plus the
            // status code are the only reliable record of what actually arrived. The status code
            // goes in the filename so a dump is identifiable without opening it.
            val payload = (response.body() ?: response.errorBody())?.bytes() ?: ByteArray(0)
            val dumpFile = writeUpdateDump(serialNumber, response.code(), payload)
            Log.i(
                "PKPassUpdate",
                "HTTP ${response.code()}, ${payload.size} bytes -> ${dumpFile.absolutePath}",
            )

            // Handle response codes
            when (response.code()) {
                200 -> {
                    // Pass updated successfully - server returns a .pkpass file (binary)
                    if (payload.isEmpty()) {
                        UpdateResult.NetworkError(WalletError.EmptyResponseBody)
                    } else {
                        try {
                            val parseResult = pkPassParser.parse(Uri.fromFile(dumpFile))

                            if (parseResult != null) {
                                val updatedPassJson =
                                    WalletJson.json.decodeFromString<PKPassJson>(parseResult.pass.rawData)
                                UpdateResult.Updated(updatedPassJson)
                            } else {
                                UpdateResult.NetworkError(WalletError.FailedToParseUpdatedPass)
                            }
                        } catch (e: Exception) {
                            Log.e("PKPassUpdate", "Failed to read update at ${dumpFile.absolutePath}", e)
                            UpdateResult.NetworkError(WalletError.FailedToParseUpdatedPass)
                        }
                    }
                }
                304 -> {
                    // Pass not modified
                    UpdateResult.NotModified
                }
                401 -> {
                    // Unauthorized
                    UpdateResult.Unauthorized
                }
                404 -> {
                    // Pass deleted/voided
                    UpdateResult.Deleted
                }
                429 -> {
                    // Rate limited by the issuer
                    UpdateResult.NetworkError(WalletError.TooManyRequests)
                }
                else -> {
                    // Other error codes
                    UpdateResult.NetworkError(
                        WalletError.ServerReturned("${response.code()}: ${response.message()}"),
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e("PKPassUpdate", "RequestTimedOut", e)
            UpdateResult.NetworkError(WalletError.RequestTimedOut)
        } catch (e: IOException) {
            Log.e("PKPassUpdate", "NoInternet", e)
            UpdateResult.NetworkError(WalletError.NoInternetConnection)
        } catch (e: Exception) {
            Log.e("PKPassUpdate", "Unknown error", e)
            UpdateResult.NetworkError(WalletError.UpdateFailed(e.walletErrorOr(WalletError.Unknown)))
        }
    }

    /**
     * Registers this device for the pass with its issuer. Call once, when the pass is added.
     *
     * Issuers commonly gate the pass body behind an existing registration and answer an
     * unregistered device with 200 and no content, so without this a pass can never be updated.
     *
     * Failures are deliberately swallowed: registration is an enabler, not the operation the user
     * asked for, and an issuer that rejects or ignores it may still serve updates.
     */
    suspend fun registerDevice(pass: Pass) {
        try {
            val passJson = WalletJson.json.decodeFromString<PKPassJson>(pass.rawData)
            val webServiceURL = passJson.webServiceURL ?: return
            val authToken = passJson.authenticationToken ?: return

            val registrationUrl = PKPassUpdateUrlResolver.resolveRegistration(
                webServiceUrl = webServiceURL,
                deviceLibraryIdentifier = deviceIdentity.deviceLibraryIdentifier,
                passTypeIdentifier = passJson.passTypeIdentifier,
                serialNumber = passJson.serialNumber,
            ) ?: return

            val body = """{"pushToken":"${deviceIdentity.pushToken}"}"""
                .toRequestBody("application/json".toMediaType())

            val response = apiService.registerDevice(
                registrationUrl = registrationUrl,
                authToken = "ApplePass $authToken",
                pushToken = body,
            )

            Log.i("PKPassUpdate", "Register ${passJson.serialNumber} -> HTTP ${response.code()}")
        } catch (e: Exception) {
            Log.w("PKPassUpdate", "Registration failed for ${pass.id}", e)
        }
    }

    /**
     * Tells the issuer this device no longer holds the pass. Best effort, for the same reason as
     * [registerDevice]: a pass is removed locally whether or not the issuer acknowledges it.
     */
    suspend fun unregisterDevice(pass: Pass) {
        try {
            val passJson = WalletJson.json.decodeFromString<PKPassJson>(pass.rawData)
            val webServiceURL = passJson.webServiceURL ?: return
            val authToken = passJson.authenticationToken ?: return

            val registrationUrl = PKPassUpdateUrlResolver.resolveRegistration(
                webServiceUrl = webServiceURL,
                deviceLibraryIdentifier = deviceIdentity.deviceLibraryIdentifier,
                passTypeIdentifier = passJson.passTypeIdentifier,
                serialNumber = passJson.serialNumber,
            ) ?: return

            val response = apiService.unregisterDevice(
                registrationUrl = registrationUrl,
                authToken = "ApplePass $authToken",
            )

            Log.i("PKPassUpdate", "Unregister ${passJson.serialNumber} -> HTTP ${response.code()}")
        } catch (e: Exception) {
            Log.w("PKPassUpdate", "Unregistration failed", e)
        }
    }

    /**
     * Writes the raw update response to the app's external files directory and prunes older dumps.
     * Written unconditionally — any status code, any size, including zero — so an empty or
     * unexpected response is as visible on disk as a valid one. Falls back to the cache directory
     * when external storage is unavailable.
     */
    private fun writeUpdateDump(serialNumber: String, statusCode: Int, bytes: ByteArray): File {
        val dumpDir = File(context.getExternalFilesDir(null) ?: context.cacheDir, UPDATE_DUMP_DIR)
        dumpDir.mkdirs()

        dumpDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_UPDATE_DUMPS)
            ?.forEach { it.delete() }

        val safeSerial = serialNumber.replace(Regex("[^A-Za-z0-9._-]"), "_").take(48)
        val dumpFile = File(dumpDir, "${System.currentTimeMillis()}_${statusCode}_$safeSerial.bin")
        FileOutputStream(dumpFile).use { it.write(bytes) }
        return dumpFile
    }

    private companion object {
        const val UPDATE_DUMP_DIR = "update-dumps"
        const val MAX_UPDATE_DUMPS = 20
    }
}
