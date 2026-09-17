package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.archive.WalletArchiveImportResult
import com.bazyak.walletplus.data.model.WalletError

internal fun WalletError.toMessage(context: Context): String = when (this) {
    WalletError.UnsupportedPassFormat -> context.getString(R.string.unsupported_pass_format)
    WalletError.FailedToParsePassFile -> context.getString(R.string.failed_to_parse_pass_file)
    WalletError.PassNotFound -> context.getString(R.string.pass_not_found)
    WalletError.TemporaryAssetsNotFound -> context.getString(R.string.temporary_assets_not_found)
    is WalletError.NoExporterAvailable -> context.getString(R.string.no_exporter_available, format)
    WalletError.ExportFailed -> context.getString(R.string.export_failed)
    WalletError.FailedToExportWallet -> context.getString(R.string.failed_to_export_wallet)
    WalletError.FailedToLoadPass -> context.getString(R.string.failed_to_load_pass)
    WalletError.FailedToAddPass -> context.getString(R.string.failed_to_add_pass)
    WalletError.FailedToDownloadPass -> context.getString(R.string.failed_to_download_pass)
    WalletError.FailedToSharePass -> context.getString(R.string.failed_to_share_pass)
    WalletError.FailedToImportWalletArchive -> context.getString(R.string.failed_to_import_wallet_archive)
    WalletError.PassWasDeleted -> context.getString(R.string.pass_was_deleted)
    WalletError.UpdateAuthorizationFailed -> context.getString(R.string.update_authorization_failed)
    WalletError.PassUpdatesUnsupported -> context.getString(R.string.pass_updates_unsupported)
    WalletError.FailedToParseUpdatedPass -> context.getString(R.string.failed_to_parse_updated_pass)
    WalletError.EmptyResponseBody -> context.getString(R.string.empty_response_body)
    is WalletError.ServerReturned -> context.getString(R.string.server_returned, message)
    WalletError.TooManyRequests -> context.getString(R.string.too_many_requests)
    WalletError.RequestTimedOut -> context.getString(R.string.request_timed_out)
    WalletError.NoInternetConnection -> context.getString(R.string.no_internet_connection)
    is WalletError.UpdateFailed -> context.getString(R.string.update_failed, cause.toMessage(context))
    is WalletError.DownloadFailed -> context.getString(R.string.download_failed, message)
    is WalletError.NetworkError -> context.getString(R.string.network_error, message)
    WalletError.NoPassesAvailableToExport -> context.getString(R.string.no_passes_available_to_export)
    WalletError.UnableToOpenArchive -> context.getString(R.string.unable_to_open_wallet_archive)
    WalletError.MissingManifest -> context.getString(R.string.wallet_archive_manifest_missing)
    is WalletError.UnsupportedVersion -> context.getString(R.string.unsupported_wallet_archive_version, version)
    WalletError.EmptyArchive -> context.getString(R.string.wallet_archive_empty)
    is WalletError.MissingMetadata -> context.getString(R.string.pass_metadata_missing, passId)
    is WalletError.MetadataMismatch -> context.getString(R.string.pass_metadata_mismatch, passId)
    is WalletError.UnsafeEntry -> context.getString(R.string.unsafe_wallet_archive_entry, entryName)
    WalletError.NoPassesImported -> context.getString(R.string.no_passes_could_be_imported)
    WalletError.MissingPkpassPayload -> context.getString(R.string.pkpass_payload_missing)
    WalletError.FailedToParsePkpassPayload -> context.getString(R.string.failed_to_parse_pkpass_payload)
    WalletError.PkpassPayloadIdMismatch -> context.getString(R.string.pkpass_payload_id_mismatch)
    WalletError.Unknown -> context.getString(R.string.unknown_error)
}

internal fun WalletArchiveImportResult.localizedSummary(context: Context): String = buildString {
    val passWord = if (importedCount == 1) {
        context.getString(R.string.pass_singular)
    } else {
        context.getString(R.string.pass_plural)
    }
    append(context.getString(R.string.imported_passes_summary, importedCount, passWord))
    if (replacedCount > 0) {
        append(" (")
        append(context.getString(R.string.replaced_suffix, replacedCount))
        append(")")
    }
    if (failedCount > 0) {
        append(", ")
        append(context.getString(R.string.failed_suffix, failedCount))
    }
}
