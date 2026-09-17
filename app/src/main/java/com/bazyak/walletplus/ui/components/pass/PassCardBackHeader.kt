package com.bazyak.walletplus.ui.components.pass

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R as AppR
import com.bazyak.walletplus.designsystem.R as DesignR
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import java.io.File

/**
 * Reusable header component for pass card back sides.
 *
 * Displays a logo/icon on the left (if provided) and action buttons (Share, Delete) on the right.
 * Used by both PKPass and Custom pass back sides.
 *
 * @param logoPath Optional path to the logo image file (PKPass uses this)
 * @param iconPath Optional path to the icon image file (fallback if no logo)
 * @param textColor Color for the action button icons
 * @param onShareClick Callback invoked when share button is clicked
 * @param onDeleteClick Callback invoked when delete button is clicked
 * @param modifier Optional modifier for the root Row
 */
@Composable
fun PassCardBackHeader(
    logoPath: String?,
    iconPath: String?,
    textColor: Color,
    onShareClick: (() -> Unit)?,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.mediumLarge, vertical = MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo (prioritize logo over icon) - only for PKPass
        val imagePath = logoPath ?: iconPath
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = stringResource(AppR.string.logo),
                        modifier = Modifier
                            .height(40.dp)
                            .widthIn(max = 120.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Share button
        onShareClick?.let { action ->
            IconButton(
                onClick = action,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.share),
                    contentDescription = stringResource(AppR.string.share),
                    tint = textColor,
                )
            }
        }

        // Delete button
        IconButton(
            onClick = onDeleteClick,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.delete),
                contentDescription = stringResource(AppR.string.delete),
                tint = textColor,
            )
        }
    }
}
