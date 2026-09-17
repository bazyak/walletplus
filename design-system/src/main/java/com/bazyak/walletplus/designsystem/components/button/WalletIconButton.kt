package com.bazyak.walletplus.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bazyak.walletplus.designsystem.foundation.spacing.LocalSpacing
import com.bazyak.walletplus.designsystem.theme.WalletTheme

/**
 * Icon button for the Wallet design system.
 *
 * A Material 3 icon button with consistent styling.
 * Typically used for toolbar actions, navigation icons, and secondary actions.
 *
 * Usage:
 * ```
 * WalletIconButton(
 *     onClick = { /* action */ }
 * ) {
 *     Icon(
 *         imageVector = Icons.Default.Add,
 *         contentDescription = "Add"
 *     )
 * }
 * ```
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param colors The colors to use for the button
 * @param content The button content (typically an Icon)
 */
@Composable
fun WalletIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults
        .iconButtonColors()
        .copy(contentColor = MaterialTheme.colorScheme.onSurface),
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        content = content,
    )
}

// ========== Previews ==========

@Preview(name = "Wallet Icon Button - Light")
@Composable
private fun WalletIconButtonPreviewLight() {
    WalletTheme(darkTheme = false) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
        ) {
            WalletIconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                )
            }
        }
    }
}

@Preview(name = "Wallet Icon Button - Dark")
@Composable
private fun WalletIconButtonPreviewDark() {
    WalletTheme(darkTheme = true) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
        ) {
            WalletIconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                )
            }
        }
    }
}
