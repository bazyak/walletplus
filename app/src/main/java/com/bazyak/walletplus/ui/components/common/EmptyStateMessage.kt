package com.bazyak.walletplus.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.designsystem.R
import com.bazyak.walletplus.designsystem.foundation.typography.textStyles

/**
 * Reusable empty state message component.
 *
 * Displays an icon and message centered in its container, typically used
 * when there's no content to display (e.g., "No additional information").
 *
 * @param message The text message to display
 * @param tint The color to apply to both icon and text (with appropriate alpha)
 * @param modifier Optional modifier for the root Column
 */
@Composable
fun EmptyStateMessage(message: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.info),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.textStyles.bodySecondary,
            color = tint,
            textAlign = TextAlign.Center,
        )
    }
}
