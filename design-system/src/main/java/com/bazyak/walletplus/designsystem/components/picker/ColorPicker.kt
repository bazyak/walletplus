package com.bazyak.walletplus.designsystem.components.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.designsystem.foundation.color.PassColorPalette
import com.bazyak.walletplus.designsystem.foundation.color.createCustomPassGradient

/**
 * Color picker with circular swatches in a horizontally scrolling row.
 *
 * Displays a list of pass color palettes as circular swatches with gradients.
 * Used in the custom pass builder for selecting card colors.
 *
 * Usage:
 * ```
 * ColorPicker(
 *     colors = MaterialTheme.walletColors,
 *     selectedIndex = 0,
 *     onColorSelected = { index -> /* handle selection */ }
 * )
 * ```
 *
 * @param colors List of pass color palettes to display
 * @param selectedIndex The currently selected color index
 * @param onColorSelected Callback when a color is selected
 * @param modifier Modifier to be applied to the picker
 */
@Composable
fun ColorPicker(
    colors: List<PassColorPalette>,
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        colors.forEachIndexed { index, palette ->
            ColorCircle(
                palette = palette,
                isSelected = index == selectedIndex,
                onClick = { onColorSelected(index) },
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

/**
 * Individual circular color swatch with gradient.
 *
 * Displays a single color palette as a circular swatch with a gradient.
 * Shows a primary border when selected, a subtle outline otherwise.
 *
 * @param palette The color palette to display
 * @param isSelected Whether this color is currently selected
 * @param onClick Callback when the circle is clicked
 * @param modifier Modifier to be applied to the circle
 */
@Composable
fun ColorCircle(palette: PassColorPalette, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gradient = createCustomPassGradient(palette.background, palette.foreground)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(gradient)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape,
                    )
                },
            )
            .clickable(onClick = onClick),
    )
}
