package com.bazyak.walletplus.designsystem.components.feedback

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Snackbar status types for the Wallet design system.
 */
enum class SnackbarStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INFO,
}

/**
 * Custom snackbar for the Wallet design system.
 *
 * A Material 3 snackbar with status-aware colors.
 *
 * Usage:
 * ```
 * WalletSnackbar(
 *     snackbarData = snackbarData,
 *     status = SnackbarStatus.SUCCESS
 * )
 * ```
 */
@Composable
fun WalletSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    status: SnackbarStatus = SnackbarStatus.INFO,
    shape: Shape = MaterialTheme.shapes.small,
) {
    val containerColor = when (status) {
        SnackbarStatus.SUCCESS -> Color(0xFF4CAF50) // Green
        SnackbarStatus.ERROR -> MaterialTheme.colorScheme.error
        SnackbarStatus.LOADING -> MaterialTheme.colorScheme.primary
        SnackbarStatus.INFO -> MaterialTheme.colorScheme.inverseSurface
    }

    val contentColor = when (status) {
        SnackbarStatus.SUCCESS -> Color.White
        SnackbarStatus.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarStatus.LOADING -> MaterialTheme.colorScheme.onPrimary
        SnackbarStatus.INFO -> MaterialTheme.colorScheme.inverseOnSurface
    }

    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = contentColor,
    )
}

/**
 * Simple snackbar for quick messages.
 */
@Composable
fun WalletSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    status: SnackbarStatus = SnackbarStatus.INFO,
    shape: Shape = MaterialTheme.shapes.small,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val containerColor = when (status) {
        SnackbarStatus.SUCCESS -> Color(0xFF4CAF50) // Green
        SnackbarStatus.ERROR -> MaterialTheme.colorScheme.error
        SnackbarStatus.LOADING -> MaterialTheme.colorScheme.primary
        SnackbarStatus.INFO -> MaterialTheme.colorScheme.inverseSurface
    }

    val contentColor = when (status) {
        SnackbarStatus.SUCCESS -> Color.White
        SnackbarStatus.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarStatus.LOADING -> MaterialTheme.colorScheme.onPrimary
        SnackbarStatus.INFO -> MaterialTheme.colorScheme.inverseOnSurface
    }

    Snackbar(
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        action = if (actionLabel != null && onActionClick != null) {
            {
                androidx.compose.material3.TextButton(onClick = onActionClick) {
                    androidx.compose.material3.Text(
                        text = actionLabel,
                        color = contentColor,
                    )
                }
            }
        } else {
            null
        },
    ) {
        androidx.compose.material3.Text(message)
    }
}
