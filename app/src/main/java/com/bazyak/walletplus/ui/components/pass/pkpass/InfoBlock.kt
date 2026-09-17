package com.bazyak.walletplus.ui.components.pass.pkpass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.designsystem.foundation.typography.textStyles
import com.bazyak.walletplus.ui.components.HtmlText

/**
 * Info block for displaying back field with HTML content.
 *
 * These blocks intentionally use a fixed white surface to replicate the "paper card on
 * coloured pass background" appearance of Apple Wallet. Theme surfaces are NOT used here
 * because the containing card already provides the coloured background.
 */
@Composable
internal fun InfoBlock(title: String, htmlContent: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ColorTokens.pkPassBackSurface,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.mediumLarge),
        ) {
            // Block title/label (only if not blank)
            if (title.isNotBlank()) {
                SelectionContainer {
                    Text(
                        text = title,
                        style = MaterialTheme.textStyles.labelSecondary,
                        color = ColorTokens.pkPassBackContent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

            // HTML content with clickable links - selectable
            SelectionContainer {
                HtmlText(
                    html = htmlContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.pkPassBackContent,
                    linkColor = ColorTokens.pkPassBackLink,
                )
            }
        }
    }
}
