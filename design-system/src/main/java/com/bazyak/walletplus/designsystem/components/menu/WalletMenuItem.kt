package com.bazyak.walletplus.designsystem.components.menu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens

/**
 * Menu item for the Wallet design system.
 *
 * A Material 3 dropdown menu item with consistent styling.
 * Typically used within WalletDropdownMenu.
 *
 * Usage:
 * ```
 * WalletMenuItem(
 *     text = { Text("Delete") },
 *     onClick = { /* action */ },
 *     leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
 * )
 * ```
 */
@Composable
fun WalletMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(
        textColor = ColorTokens.contentPrimary,
        leadingIconColor = ColorTokens.contentSecondary,
        trailingIconColor = ColorTokens.contentSecondary,
        disabledTextColor = ColorTokens.contentSecondary.copy(alpha = 0.38f),
        disabledLeadingIconColor = ColorTokens.contentSecondary.copy(alpha = 0.38f),
        disabledTrailingIconColor = ColorTokens.contentSecondary.copy(alpha = 0.38f),
    ),
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
    )
}
