package com.bazyak.walletplus.ui.components.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens
import com.bazyak.walletplus.designsystem.theme.WalletTheme

/**
 * Reusable delete confirmation dialog for passes.
 *
 * Displays a confirmation dialog when the user attempts to delete a pass.
 * Handles the delete action and dismisses the dialog and parent screen on confirmation.
 *
 * @param showDialog Whether the dialog is currently visible
 * @param onDelete Callback invoked when user decides to delete a pass
 * @param onDismiss Callback invoked when the dialog is dismissed (both cancel and after delete)
 */
@Composable
fun PassDeleteDialog(showDialog: Boolean, onDelete: () -> Unit, onDismiss: () -> Unit) {
    if (showDialog) {
        WalletTheme {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(text = stringResource(R.string.delete_pass_title))
                },
                text = {
                    Text(text = stringResource(R.string.delete_pass_message))
                },
                confirmButton = {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ColorTokens.error,
                        ),
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ColorTokens.brandPrimary,
                        ),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = ColorTokens.surfaceDefault,
                titleContentColor = ColorTokens.contentPrimary,
                textContentColor = ColorTokens.contentSecondary,
            )
        }
    }
}
