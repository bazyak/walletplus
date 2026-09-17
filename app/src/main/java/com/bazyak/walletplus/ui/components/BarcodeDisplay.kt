package com.bazyak.walletplus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.BarcodeFormatType
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.designsystem.foundation.typography.textStyles
import com.bazyak.walletplus.ui.utils.generateBarcodeBitmap

/**
 * Common barcode display component used in both preview and detail views.
 * Matches PKPass card styling from PassCardFront.kt.
 */
@Composable
fun BarcodeDisplay(
    barcodeValue: String,
    barcodeFormat: BarcodeFormatType,
    textColor: Color,
    modifier: Modifier = Modifier,
    altText: String? = null,
) {
    val padding = MaterialTheme.spacing.mediumLarge
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = (if (barcodeFormat.isQRCode) 200.dp else 150.dp) + padding * 2)
            .padding(all = MaterialTheme.spacing.mediumLarge),
    ) {
        val barcodeBitmap = remember(barcodeValue, barcodeFormat) {
            generateBarcodeBitmap(
                message = barcodeValue,
                format = barcodeFormat.zxingFormat,
                width = if (barcodeFormat.isQRCode) 600 else 800,
                height = if (barcodeFormat.isQRCode) 600 else 400,
            )
        }

        barcodeBitmap?.let { bitmap ->
            Surface(
                modifier = Modifier.weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.barcode),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spacing.medium),
                    contentScale = ContentScale.Fit,
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.weight(1f)
                    .background(
                        color = textColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.barcode_unavailable),
                    style = MaterialTheme.textStyles.captionSecondary,
                    color = textColor,
                )
            }
        }

        // Display altText or barcode value below
        val displayText = altText?.takeIf { it.isNotBlank() } ?: barcodeValue
        if (displayText.isNotBlank()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                text = displayText,
                style = MaterialTheme.textStyles.labelSecondary,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
