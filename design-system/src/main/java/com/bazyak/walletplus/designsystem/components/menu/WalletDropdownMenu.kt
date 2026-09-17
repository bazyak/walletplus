package com.bazyak.walletplus.designsystem.components.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens

/**
 * Dropdown menu for the Wallet design system.
 *
 * A Material 3 dropdown menu with consistent styling.
 *
 * Usage:
 * ```
 * WalletDropdownMenu(
 *     expanded = expanded,
 *     onDismissRequest = { expanded = false }
 * ) {
 *     WalletMenuItem(
 *         text = { Text("Option 1") },
 *         onClick = { /* action */ }
 *     )
 * }
 * ```
 */
@Composable
fun WalletDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier.clip(RoundedCornerShape(16.dp)),
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        properties = properties,
        content = content,
        shape = RoundedCornerShape(12.dp),
        containerColor = ColorTokens.surfaceDefault,
    )
}
