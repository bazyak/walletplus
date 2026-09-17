package com.bazyak.walletplus.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.bazyak.walletplus.designsystem.foundation.spacing.LocalSpacing
import com.bazyak.walletplus.designsystem.theme.WalletTheme

/**
 * Primary filled button for the Wallet design system.
 *
 * A Material 3 filled button with consistent styling for primary actions.
 * Uses the primary color from the theme.
 *
 * Usage:
 * ```
 * WalletFilledButton(
 *     onClick = { /* action */ }
 * ) {
 *     Text("Click Me")
 * }
 * ```
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param shape The shape of the button
 * @param contentPadding The padding to apply to the button content
 * @param content The button content (typically text and/or icon)
 */
@Composable
fun WalletFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(),
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Outlined button for the Wallet design system.
 *
 * A Material 3 outlined button with consistent styling for secondary actions.
 * Uses the primary color for the outline.
 *
 * Usage:
 * ```
 * WalletOutlinedButton(
 *     onClick = { /* action */ }
 * ) {
 *     Text("Cancel")
 * }
 * ```
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param shape The shape of the button
 * @param contentPadding The padding to apply to the button content
 * @param content The button content (typically text and/or icon)
 */
@Composable
fun WalletOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(),
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Text button for the Wallet design system.
 *
 * A Material 3 text button with consistent styling for tertiary/low-emphasis actions.
 * Has no background or border.
 *
 * Usage:
 * ```
 * WalletTextButton(
 *     onClick = { /* action */ }
 * ) {
 *     Text("Learn More")
 * }
 * ```
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param shape The shape of the button
 * @param contentPadding The padding to apply to the button content
 * @param content The button content (typically text and/or icon)
 */
@Composable
fun WalletTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(),
        contentPadding = contentPadding,
        content = content,
    )
}

// ========== Previews ==========

@Preview(name = "Wallet Buttons - Light")
@Composable
private fun WalletButtonsPreviewLight() {
    WalletTheme(darkTheme = false) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
        ) {
            WalletFilledButton(onClick = {}) {
                Text("Filled Button")
            }
            WalletOutlinedButton(onClick = {}) {
                Text("Outlined Button")
            }
            WalletTextButton(onClick = {}) {
                Text("Text Button")
            }
        }
    }
}

@Preview(name = "Wallet Buttons - Dark")
@Composable
private fun WalletButtonsPreviewDark() {
    WalletTheme(darkTheme = true) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
        ) {
            WalletFilledButton(onClick = {}) {
                Text("Filled Button")
            }
            WalletOutlinedButton(onClick = {}) {
                Text("Outlined Button")
            }
            WalletTextButton(onClick = {}) {
                Text("Text Button")
            }
        }
    }
}
