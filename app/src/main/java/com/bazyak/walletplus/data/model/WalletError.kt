package com.bazyak.walletplus.data.model

sealed interface WalletError {
    data object UnsupportedPassFormat : WalletError
    data object FailedToParsePassFile : WalletError
    data object PassNotFound : WalletError
    data object TemporaryAssetsNotFound : WalletError
    data class NoExporterAvailable(val format: String) : WalletError
    data object ExportFailed : WalletError
    data object FailedToExportWallet : WalletError
    data object FailedToLoadPass : WalletError
    data object FailedToAddPass : WalletError
    data object FailedToDownloadPass : WalletError
    data object FailedToSharePass : WalletError
    data object FailedToImportWalletArchive : WalletError

    data object PassWasDeleted : WalletError
    data object UpdateAuthorizationFailed : WalletError
    data object PassUpdatesUnsupported : WalletError
    data object FailedToParseUpdatedPass : WalletError
    data object EmptyResponseBody : WalletError
    data class ServerReturned(val message: String) : WalletError
    data object TooManyRequests : WalletError
    data object RequestTimedOut : WalletError
    data object NoInternetConnection : WalletError
    data class UpdateFailed(val cause: WalletError) : WalletError
    data class DownloadFailed(val message: String) : WalletError
    data class NetworkError(val message: String) : WalletError

    data object NoPassesAvailableToExport : WalletError
    data object UnableToOpenArchive : WalletError
    data object MissingManifest : WalletError
    data class UnsupportedVersion(val version: Int) : WalletError
    data object EmptyArchive : WalletError
    data class MissingMetadata(val passId: String) : WalletError
    data class MetadataMismatch(val passId: String) : WalletError
    data class UnsafeEntry(val entryName: String) : WalletError
    data object NoPassesImported : WalletError
    data object MissingPkpassPayload : WalletError
    data object FailedToParsePkpassPayload : WalletError
    data object PkpassPayloadIdMismatch : WalletError

    data object Unknown : WalletError
}

class WalletErrorException(val error: WalletError) : Exception(error.toString())

fun WalletError.asException(): WalletErrorException = WalletErrorException(this)

fun Throwable?.walletErrorOr(default: WalletError): WalletError = (this as? WalletErrorException)?.error ?: default
