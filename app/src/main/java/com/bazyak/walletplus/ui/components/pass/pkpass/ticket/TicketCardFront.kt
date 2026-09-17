package com.bazyak.walletplus.ui.components.pass.pkpass.ticket

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.BarcodeFormatType
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.parser.pkpass.PKField
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.BarcodeDisplay
import com.bazyak.walletplus.ui.components.common.DottedDivider
import com.bazyak.walletplus.ui.utils.rememberCardColors
import com.bazyak.walletplus.ui.utils.rememberLocalizedValue
import com.bazyak.walletplus.ui.utils.stripHtml
import com.bazyak.walletplus.ui.utils.withValues
import java.io.File

/**
 * Front side of a ticket-style pass card.
 * Uses TicketShape with semicircular notches and 60/40 split layout for info and barcode sections.
 */
@Composable
fun TicketCardFront(pass: Pass, pkPassJson: PKPassJson?, modifier: Modifier = Modifier) {
    val cardColors = rememberCardColors(pass)
    val backgroundColor = cardColors.background
    val textColor = cardColors.text
    val labelColor = cardColors.label

    val structure = pkPassJson?.let { json ->
        json.boardingPass ?: json.eventTicket ?: json.coupon ?: json.storeCard ?: json.generic
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor,
        shape = TicketShape(
            cornerRadius = 16.dp,
            notchPosition = 0.6f,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState()),
        ) {
            // INFO SECTION (60%)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
            ) {
                TicketStripImage(stripPath = pass.stripPath)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.mediumLarge),
                ) {
                    TicketHeaderRow(
                        pass = pass,
                        pkPassJson = pkPassJson,
                        headerFields = structure?.headerFields?.withValues(),
                        textColor = textColor,
                        labelColor = labelColor,
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    structure?.primaryFields?.withValues()?.let { fields ->
                        TicketFieldsRow(
                            fields = fields,
                            pkPassJson = pkPassJson,
                            textColor = textColor,
                            labelColor = labelColor,
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    structure?.secondaryFields?.withValues()?.let { fields ->
                        TicketFieldsRow(
                            fields = fields,
                            pkPassJson = pkPassJson,
                            textColor = textColor,
                            labelColor = labelColor,
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    structure?.auxiliaryFields?.withValues()?.let { fields ->
                        TicketFieldsRow(
                            fields = fields,
                            pkPassJson = pkPassJson,
                            textColor = textColor,
                            labelColor = labelColor,
                        )
                    }
                }
            }

            // DOTTED DIVIDER
            DottedDivider(modifier = Modifier.padding(horizontal = 12.dp))

            // BARCODE SECTION (40%)
            TicketBarcodeSection(
                pkPassJson = pkPassJson,
                textColor = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
            )
        }
    }
}

@Composable
private fun TicketStripImage(stripPath: String?) {
    stripPath?.let { path ->
        val stripFile = File(path)
        if (stripFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(stripFile.absolutePath)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = stringResource(R.string.strip_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

@Composable
private fun TicketHeaderRow(
    pass: Pass,
    pkPassJson: PKPassJson?,
    headerFields: List<PKField>?,
    textColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val logoPath = pass.logoPath ?: pass.iconPath
        val logoFile = File(logoPath)
        if (logoFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = stringResource(R.string.logo),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(max = 120.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        headerFields?.let { fields ->
            if (fields.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    fields.forEach { field ->
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            val localizedLabel = rememberLocalizedValue(field.label, pkPassJson)
                            val localizedValue = rememberLocalizedValue(
                                field.value?.toString(),
                                pkPassJson,
                            )
                            Text(
                                text = stripHtml(localizedLabel),
                                style = MaterialTheme.typography.labelMedium,
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                            )
                            Text(
                                text = stripHtml(localizedValue),
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketFieldsRow(
    fields: List<PKField>,
    pkPassJson: PKPassJson?,
    textColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color,
) {
    if (fields.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (fields.size) {
            1 -> Arrangement.Start
            2 -> Arrangement.SpaceBetween
            else -> Arrangement.SpaceEvenly
        },
    ) {
        fields.forEach { field ->
            Column(
                modifier = when {
                    fields.size > 2 -> Modifier.weight(1f)
                    else -> Modifier
                },
                horizontalAlignment = Alignment.Start,
            ) {
                val localizedLabel = rememberLocalizedValue(field.label, pkPassJson)
                val localizedValue = rememberLocalizedValue(field.value?.toString(), pkPassJson)
                Text(
                    text = stripHtml(localizedLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stripHtml(localizedValue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TicketBarcodeSection(
    pkPassJson: PKPassJson?,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    pkPassJson?.let { json ->
        val barcode = json.barcodes?.firstOrNull() ?: json.barcode
        barcode?.let { barcodeData ->
            BarcodeFormatType.fromPKPassFormat(barcodeData.format)?.let { format ->
                val localizedAltText = rememberLocalizedValue(barcodeData.altText, pkPassJson)
                Box(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    BarcodeDisplay(
                        barcodeValue = barcodeData.message,
                        barcodeFormat = format,
                        textColor = textColor,
                        altText = localizedAltText,
                    )
                }
            }
        }
    }
}
