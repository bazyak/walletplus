package com.bazyak.walletplus.ui.components.pass.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.BarcodeFormatType
import com.bazyak.walletplus.data.model.CustomPassJson
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.designsystem.foundation.color.createCustomPassGradient
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.BarcodeDisplay
import com.bazyak.walletplus.ui.utils.IconMapper
import com.bazyak.walletplus.ui.utils.rememberCardColors

/**
 * Front side of a custom pass card.
 * Displays: Icon + Card name (top left in row) + Barcode (bottom)
 */
@Composable
fun CustomPassCardFront(pass: Pass, customPassJson: CustomPassJson, modifier: Modifier = Modifier) {
    val cardColors = rememberCardColors(pass)
    val backgroundColor = cardColors.background
    val foregroundColor = cardColors.foreground ?: MaterialTheme.colorScheme.onSurface
    val textColor = cardColors.text

    val iconRes = IconMapper.getIconByName(customPassJson.iconName)

    CustomPassCard(
        cardName = pass.organizationName,
        icon = iconRes,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        textColor = textColor,
        barcodeValue = customPassJson.barcodeValue,
        barcodeFormat = customPassJson.barcodeFormat,
        isEditable = false,
        onCardNameChange = {},
        modifier = modifier,
    )
}

/**
 * Shared custom pass card component that can be used in both expansion and builder views.
 * Supports both editable (TextField) and non-editable (Text) card name.
 */
@Suppress("LongParameterList")
@Composable
fun CustomPassCard(
    cardName: String,
    icon: Int,
    backgroundColor: Color,
    foregroundColor: Color,
    textColor: Color,
    barcodeValue: String,
    barcodeFormat: String,
    isEditable: Boolean,
    onCardNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = createCustomPassGradient(backgroundColor, foregroundColor)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(gradient, RoundedCornerShape(16.dp)),
    ) {
        // 1. HEADER ROW: Icon + Card name (left aligned)
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
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = textColor,
            )
            Spacer(modifier = Modifier.size(MaterialTheme.spacing.extraSmall))

            if (isEditable) {
                val textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )

                TextField(
                    value = cardName,
                    onValueChange = onCardNameChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_card_name),
                            style = textStyle.copy(color = textColor.copy(alpha = 0.5f)),
                        )
                    },
                    textStyle = textStyle,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = textColor,
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            } else {
                Text(
                    text = cardName,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Flexible spacer to push barcode to center of remaining space
        Spacer(modifier = Modifier.weight(1f))

        // 2. BARCODE SECTION - Centered in bottom portion
        BarcodeDisplay(
            barcodeValue = barcodeValue,
            barcodeFormat = BarcodeFormatType.fromName(barcodeFormat),
            textColor = textColor,
        )

        // Flexible spacer to balance and center barcode
        Spacer(modifier = Modifier.weight(1f))
    }
}
