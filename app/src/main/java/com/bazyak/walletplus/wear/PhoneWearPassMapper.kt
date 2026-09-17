package com.bazyak.walletplus.wear

import android.content.Context
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.BarcodeFormatType
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassData
import com.bazyak.walletplus.data.model.PassFormat
import com.bazyak.walletplus.data.model.getPassData
import com.bazyak.walletplus.data.parser.pkpass.LocalizationResolver
import com.bazyak.walletplus.data.parser.pkpass.PKBarcode
import com.bazyak.walletplus.data.parser.pkpass.PKField
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.data.parser.pkpass.PKPassLocalizations
import com.bazyak.walletplus.data.parser.pkpass.PKPassStructure
import com.bazyak.walletplus.wearsync.WearBarcodeFormat
import com.bazyak.walletplus.wearsync.WearPassAssetRef
import com.bazyak.walletplus.wearsync.WearPassAssetType
import com.bazyak.walletplus.wearsync.WearPassBarcode
import com.bazyak.walletplus.wearsync.WearPassField
import com.bazyak.walletplus.wearsync.WearPassFieldSection
import com.bazyak.walletplus.wearsync.WearPassSnapshot
import java.io.File
import java.util.Locale

fun Pass.toWearPassSnapshot(context: Context): WearPassSnapshot? = toWearPassSnapshot(
    formatLabel = context.getString(R.string.format),
    locale = context.resources.configuration.locales[0] ?: Locale.getDefault(),
)

internal fun Pass.toWearPassSnapshot(formatLabel: String, locale: Locale = Locale.getDefault()): WearPassSnapshot? =
    runCatching {
        when (val passData = getPassData()) {
            is PassData.PKPass -> toPkPassSnapshot(passData.pkPassJson, locale)
            is PassData.Custom -> WearPassSnapshot(
                id = id,
                format = format.name,
                title = organizationName,
                subtitle = description,
                category = category.name,
                backgroundColor = backgroundColor,
                foregroundColor = foregroundColor,
                labelColor = labelColor,
                displayOrder = displayOrder,
                barcode = WearPassBarcode(
                    value = passData.customPassJson.barcodeValue,
                    format = BarcodeFormatType.fromName(passData.customPassJson.barcodeFormat).toWearFormat(),
                ),
                customIconName = passData.customPassJson.iconName,
                fields = listOf(
                    WearPassField(
                        key = "format",
                        label = formatLabel,
                        value = passData.customPassJson.barcodeFormat,
                        section = WearPassFieldSection.METADATA,
                    ),
                ),
                assets = wearAssetRefs(),
            )
        }
    }.getOrNull()

private fun Pass.toPkPassSnapshot(pkPassJson: PKPassJson, locale: Locale): WearPassSnapshot {
    val structure = pkPassJson.passStructure()
    val barcode = (pkPassJson.barcodes?.firstOrNull() ?: pkPassJson.barcode)?.toWearBarcode()

    return WearPassSnapshot(
        id = id,
        format = PassFormat.PKPASS.name,
        title = pkPassJson.logoText?.takeIf { it.isNotBlank() } ?: organizationName,
        subtitle = description,
        category = category.name,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        labelColor = labelColor,
        displayOrder = displayOrder,
        barcode = barcode,
        fields = structure?.wearFields(pkPassJson.localizations, locale).orEmpty(),
        assets = wearAssetRefs(),
    )
}

private fun PKPassJson.passStructure(): PKPassStructure? = boardingPass ?: eventTicket ?: coupon ?: storeCard ?: generic

private fun PKPassStructure.wearFields(localizations: PKPassLocalizations?, locale: Locale): List<WearPassField> =
    buildList {
        addFields(headerFields, WearPassFieldSection.HEADER, localizations, locale)
        addFields(primaryFields, WearPassFieldSection.PRIMARY, localizations, locale)
        addFields(secondaryFields, WearPassFieldSection.SECONDARY, localizations, locale)
        addFields(auxiliaryFields, WearPassFieldSection.AUXILIARY, localizations, locale)
        addFields(backFields, WearPassFieldSection.BACK, localizations, locale)
    }

private fun MutableList<WearPassField>.addFields(
    fields: List<PKField>?,
    section: WearPassFieldSection,
    localizations: PKPassLocalizations?,
    locale: Locale,
) {
    fields.orEmpty().forEach { field ->
        val value = field.value?.takeIf { it.isNotBlank() } ?: return@forEach
        add(
            WearPassField(
                key = field.key,
                label = LocalizationResolver.resolveLocalizedValue(field.label, localizations, locale),
                value = LocalizationResolver.resolveLocalizedValue(value, localizations, locale),
                section = section,
            ),
        )
    }
}

private fun PKBarcode.toWearBarcode(): WearPassBarcode? {
    val wearFormat = when (format) {
        "PKBarcodeFormatQR" -> WearBarcodeFormat.QR_CODE
        "PKBarcodeFormatPDF417" -> WearBarcodeFormat.PDF_417
        "PKBarcodeFormatAztec" -> WearBarcodeFormat.AZTEC
        "PKBarcodeFormatCode128" -> WearBarcodeFormat.CODE_128
        else -> null
    } ?: return null

    return WearPassBarcode(
        value = message,
        format = wearFormat,
        altText = altText,
    )
}

private fun BarcodeFormatType.toWearFormat(): WearBarcodeFormat = when (this) {
    BarcodeFormatType.QR_CODE -> WearBarcodeFormat.QR_CODE
    BarcodeFormatType.CODE_128 -> WearBarcodeFormat.CODE_128
    BarcodeFormatType.CODE_39 -> WearBarcodeFormat.CODE_39
    BarcodeFormatType.CODE_93 -> WearBarcodeFormat.CODE_93
    BarcodeFormatType.CODABAR -> WearBarcodeFormat.CODABAR
    BarcodeFormatType.DATA_MATRIX -> WearBarcodeFormat.DATA_MATRIX
    BarcodeFormatType.EAN_13 -> WearBarcodeFormat.EAN_13
    BarcodeFormatType.EAN_8 -> WearBarcodeFormat.EAN_8
    BarcodeFormatType.ITF -> WearBarcodeFormat.ITF
    BarcodeFormatType.UPC_A -> WearBarcodeFormat.UPC_A
    BarcodeFormatType.UPC_E -> WearBarcodeFormat.UPC_E
    BarcodeFormatType.PDF_417 -> WearBarcodeFormat.PDF_417
    BarcodeFormatType.AZTEC -> WearBarcodeFormat.AZTEC
}

internal fun Pass.assetFile(type: WearPassAssetType): File? = when (type) {
    WearPassAssetType.ICON -> iconPath
    WearPassAssetType.LOGO -> logoPath
}
    ?.takeIf { it.isNotBlank() }
    ?.let(::File)
    ?.takeIf { it.exists() && it.isFile }

private fun Pass.wearAssetRefs(): List<WearPassAssetRef> = WearPassAssetType.entries.mapNotNull { type ->
    assetFile(type)?.let { file ->
        WearPassAssetRef(
            name = "$id-${type.name.lowercase()}-${file.lastModified()}",
            type = type,
        )
    }
}
