package com.bazyak.walletplus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.builder.CustomPassBuilder
import com.bazyak.walletplus.designsystem.components.button.WalletFilledButton
import com.bazyak.walletplus.designsystem.components.button.WalletOutlinedButton
import com.bazyak.walletplus.designsystem.components.navigation.WalletTopAppBar
import com.bazyak.walletplus.designsystem.components.picker.ColorPicker
import com.bazyak.walletplus.designsystem.foundation.color.walletColors
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.pass.custom.CustomPassCard
import com.bazyak.walletplus.ui.components.picker.IconPicker
import com.bazyak.walletplus.ui.utils.IconMapper
import com.bazyak.walletplus.ui.viewmodel.CustomPassBuilderViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Screen for building custom passes from scanned barcodes.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomPassBuilderScreen(
    barcodeValue: String,
    barcodeFormat: String,
    onCancel: () -> Unit,
    onPassCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomPassBuilderViewModel = koinViewModel(),
) {
    var cardName by remember { mutableStateOf("") }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var selectedColorIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val passColors = MaterialTheme.walletColors

    Scaffold(
        topBar = {
            WalletTopAppBar(
                title = { Text(stringResource(R.string.create_card)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(id = com.bazyak.walletplus.designsystem.R.drawable.cross),
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            // Preview card at the top with editable name
            val (_, selectedIconRes) = IconMapper.availableIcons[selectedIconIndex]
            val colorPalette = passColors[selectedColorIndex]
            val backgroundColor = colorPalette.background
            val foregroundColor = colorPalette.foreground
            val textColor = colorPalette.foreground

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CustomPassCard(
                    cardName = cardName,
                    onCardNameChange = { cardName = it },
                    icon = selectedIconRes,
                    backgroundColor = backgroundColor,
                    foregroundColor = foregroundColor,
                    textColor = textColor,
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    isEditable = true,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.25f)
                        .padding(vertical = MaterialTheme.spacing.medium)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // Icon picker
            Text(
                text = stringResource(R.string.icon),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumLarge),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            IconPicker(
                icons = IconMapper.availableIcons,
                selectedIndex = selectedIconIndex,
                onIconSelected = { selectedIconIndex = it },
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // Color picker
            Text(
                text = stringResource(R.string.color),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumLarge),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            ColorPicker(
                colors = passColors,
                selectedIndex = selectedColorIndex,
                onColorSelected = { selectedColorIndex = it },
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.mediumLarge),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                WalletOutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                WalletFilledButton(
                    onClick = {
                        if (cardName.isNotBlank()) {
                            val selectedColorPalette = passColors[selectedColorIndex]
                            val (iconName, _) = IconMapper.availableIcons[selectedIconIndex]
                            val pass = CustomPassBuilder.createCustomPass(
                                cardName = cardName,
                                barcodeValue = barcodeValue,
                                barcodeFormat = barcodeFormat,
                                iconName = iconName,
                                backgroundColor = String.format(
                                    "#%06X",
                                    0xFFFFFF and selectedColorPalette.background.toArgb(),
                                ),
                                foregroundColor = String.format(
                                    "#%06X",
                                    0xFFFFFF and selectedColorPalette.foreground.toArgb(),
                                ),
                                labelColor = String.format(
                                    "#%06X",
                                    0xFFFFFF and selectedColorPalette.foreground.toArgb(),
                                ),
                            )
                            viewModel.createCustomPass(pass)
                            onPassCreated()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = cardName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.add_to_wallet))
                }
            }
        }
    }
}
