package com.bazyak.walletplus.ui.components.pass.pkpass

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.BarcodeDisplay
import com.bazyak.walletplus.ui.utils.rememberCardColors
import com.bazyak.walletplus.ui.utils.rememberLocalizedValue
import com.bazyak.walletplus.ui.utils.stripHtml
import com.bazyak.walletplus.ui.utils.withValues
import java.io.File

/**
 * Front side of the pass card.
 */
@Composable
fun PassCardFront(pass: Pass, pkPassJson: PKPassJson?, modifier: Modifier = Modifier) {
    val cardColors = rememberCardColors(pass)
    val backgroundColor = cardColors.background
    val textColor = cardColors.text
    val labelColor = cardColors.label

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor, RoundedCornerShape(16.dp)),
        // .verticalScroll(state = rememberScrollState()),
    ) {
        // 1. HEADER ROW: Logo (left) + Header Fields (right)
        val structure = pkPassJson?.let { json ->
            json.boardingPass ?: json.eventTicket ?: json.coupon ?: json.storeCard ?: json.generic
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.spacing.mediumLarge,
                    end = MaterialTheme.spacing.large,
                    top = MaterialTheme.spacing.medium,
                    bottom = MaterialTheme.spacing.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Logo (prioritize logo over icon)
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

            // Header fields (right-aligned in a row)
            structure?.headerFields?.withValues()?.let { headerFields ->
                if (headerFields.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.Top,
                    ) {
                        headerFields.forEach { field ->
                            Column(
                                horizontalAlignment = Alignment.End,
                            ) {
                                val localizedLabel = rememberLocalizedValue(field.label, pkPassJson)
                                val localizedValue = rememberLocalizedValue(field.value?.toString(), pkPassJson)
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

        // 2. STRIP IMAGE (if present)
        pass.stripPath?.let { stripPath ->
            val stripFile = File(stripPath)
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

        // 3. INFO SECTION: Primary and Secondary fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.mediumLarge, vertical = MaterialTheme.spacing.medium),
        ) {
            // Primary fields
            structure?.primaryFields?.withValues()?.let { fields ->
                if (fields.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = when (fields.size) {
                            1 -> Arrangement.Start
                            2 -> Arrangement.SpaceBetween
                            else -> Arrangement.SpaceEvenly
                        },
                    ) {
                        fields.forEachIndexed { index, field ->
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
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                }
            }

            // Secondary fields
            structure?.secondaryFields?.withValues()?.let { fields ->
                if (fields.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = when (fields.size) {
                            1 -> Arrangement.Start
                            2 -> Arrangement.SpaceBetween
                            else -> Arrangement.SpaceEvenly
                        },
                    ) {
                        fields.forEachIndexed { index, field ->
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
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            pkPassJson?.let { json ->
                val barcode = json.barcodes?.firstOrNull() ?: json.barcode
                barcode?.let { barcodeData ->
                    BarcodeFormatType.fromPKPassFormat(barcodeData.format)?.let { format ->
                        val localizedAltText = rememberLocalizedValue(barcodeData.altText, pkPassJson)
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
}
