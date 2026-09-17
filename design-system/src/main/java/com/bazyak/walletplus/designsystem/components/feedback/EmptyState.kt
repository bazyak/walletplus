package com.bazyak.walletplus.designsystem.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.bazyak.walletplus.designsystem.foundation.spacing.LocalSpacing

/**
 * Empty state component for the Wallet design system.
 *
 * Displays an icon and message when there's no content to show.
 * Used for empty lists, search results, etc.
 *
 * Usage:
 * ```
 * EmptyState(
 *     icon = Icons.Default.CardGiftcard,
 *     message = "No passes yet. Add your first pass!"
 * )
 * ```
 *
 * @param icon The icon to display
 * @param message The message to display
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun EmptyState(icon: ImageVector, message: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier.padding(spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(spacing.large),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
