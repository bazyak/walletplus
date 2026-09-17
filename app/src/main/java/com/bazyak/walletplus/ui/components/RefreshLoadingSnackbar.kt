package com.bazyak.walletplus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.RefreshStatus
import com.bazyak.walletplus.designsystem.components.feedback.WalletCircularProgressIndicator
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing

/**
 * Custom snackbar for showing refresh status.
 * Displays loading, success, or error states with appropriate styling.
 */
@Composable
fun RefreshLoadingSnackbar(refreshStatus: RefreshStatus, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = refreshStatus !is RefreshStatus.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        val context = LocalContext.current
        val (backgroundColor, textColor, message, showProgress) = when (refreshStatus) {
            is RefreshStatus.Loading -> {
                val msg = if (refreshStatus.passId == null) {
                    context.getString(R.string.refreshing_all_passes)
                } else {
                    context.getString(R.string.refreshing_pass)
                }
                Tuple4(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    msg,
                    true,
                )
            }
            is RefreshStatus.Success -> {
                val msg = if (refreshStatus.updatedCount > 0) {
                    context.getString(R.string.passes_updated, refreshStatus.updatedCount)
                } else {
                    context.getString(R.string.passes_up_to_date)
                }
                Tuple4(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer,
                    msg,
                    false,
                )
            }
            is RefreshStatus.Error -> {
                Tuple4(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    refreshStatus.message,
                    false,
                )
            }
            RefreshStatus.Idle -> {
                // Not visible, but need to provide values
                Tuple4(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.onSurface,
                    "",
                    false,
                )
            }
        }

        Surface(
            color = backgroundColor,
            shape = CircleShape,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(MaterialTheme.spacing.mediumLarge),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.mediumLarge,
                    vertical = MaterialTheme.spacing.medium,
                ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showProgress) {
                    WalletCircularProgressIndicator(
                        modifier = Modifier.size(MaterialTheme.spacing.large),
                        color = textColor,
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
            }
        }
    }
}

/**
 * Helper data class for destructuring multiple values.
 */
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
