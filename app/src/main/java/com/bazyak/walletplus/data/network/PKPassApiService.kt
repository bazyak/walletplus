package com.bazyak.walletplus.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit API service for PKPass update protocol.
 * Based on Apple Wallet Pass specification.
 */
interface PKPassApiService {

    /**
     * Gets pass update from server.
     *
     * Endpoint: either the provider's complete update URL or
     * GET {webServiceURL}/v1/passes/{passTypeIdentifier}/{serialNumber}
     * Header: Authorization: ApplePass {authenticationToken}
     *
     * Response codes:
     * - 200: Pass updated, returns new .pkpass file (binary data)
     * - 304: Pass not modified
     * - 401: Unauthorized (invalid token)
     * - 404: Pass deleted/voided
     *
     * @param updateUrl Resolved absolute URL for this pass update request
     * @param authToken The authentication token (format: "ApplePass {token}")
     * @return Response containing the updated pass file or appropriate status code
     */
    @GET
    suspend fun getPassUpdate(
        @Url updateUrl: String,
        @Header("Authorization") authToken: String,
    ): Response<ResponseBody>

    /**
     * Registers this device for updates to a pass.
     *
     * Endpoint: POST {webServiceURL}/v1/devices/{deviceLibraryIdentifier}
     *           /registrations/{passTypeIdentifier}/{serialNumber}
     * Header: Authorization: ApplePass {authenticationToken}
     * Body: {"pushToken": "..."}
     *
     * Response codes:
     * - 201: Registration created
     * - 200: Device was already registered
     * - 401: Unauthorized
     *
     * Issuers commonly treat a registration as the signal that someone is actually carrying this
     * pass, and only start serving pass bodies once one exists. Without it, [getPassUpdate] can
     * keep answering 200 with an empty body forever.
     */
    @POST
    suspend fun registerDevice(
        @Url registrationUrl: String,
        @Header("Authorization") authToken: String,
        @Body pushToken: RequestBody,
    ): Response<ResponseBody>

    /**
     * Unregisters this device so the issuer stops tracking a pass we no longer hold.
     *
     * Endpoint: DELETE {webServiceURL}/v1/devices/{deviceLibraryIdentifier}
     *           /registrations/{passTypeIdentifier}/{serialNumber}
     */
    @DELETE
    suspend fun unregisterDevice(
        @Url registrationUrl: String,
        @Header("Authorization") authToken: String,
    ): Response<ResponseBody>
}
