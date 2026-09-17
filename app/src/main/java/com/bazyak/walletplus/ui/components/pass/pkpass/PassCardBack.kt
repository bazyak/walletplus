package com.bazyak.walletplus.ui.components.pass.pkpass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassFormat
import com.bazyak.walletplus.data.model.ShareStatus
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.common.EmptyStateMessage
import com.bazyak.walletplus.ui.components.common.PassDeleteDialog
import com.bazyak.walletplus.ui.components.pass.PassCardBackHeader
import com.bazyak.walletplus.ui.utils.rememberCardColors
import com.bazyak.walletplus.ui.utils.rememberLocalizedValue
import com.bazyak.walletplus.ui.utils.sharePassFile
import com.bazyak.walletplus.ui.viewmodel.PassGridViewModel
import kotlinx.coroutines.launch

/**
 * Back side of the pass card.
 */
@Composable
fun PassCardBack(
    pass: Pass,
    pkPassJson: PKPassJson?,
    viewModel: PassGridViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColors = rememberCardColors(pass)
    val backgroundColor = cardColors.background
    val textColor = cardColors.text

    val shareStatus by viewModel.shareStatus.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle share status changes
    LaunchedEffect(shareStatus) {
        when (val status = shareStatus) {
            is ShareStatus.Success -> {
                if (status.passId == pass.id) {
                    sharePassFile(context, status.exportResult)
                    viewModel.resetShareStatus()
                }
            }
            is ShareStatus.Error -> {
                if (status.passId == pass.id) {
                    // Log error silently (matches app pattern)
                    viewModel.resetShareStatus()
                }
            }
            else -> { /* Idle or Loading */ }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor, RoundedCornerShape(16.dp)),
    ) {
        // 1. HEADER ROW: Logo (left) + Action Buttons (right)
        PassCardBackHeader(
            logoPath = pass.logoPath,
            iconPath = pass.iconPath,
            textColor = textColor,
            onShareClick = { viewModel.prepareSharePass(pass.id) }
                .takeIf { pass.format == PassFormat.PKPASS },
            onDeleteClick = { showDeleteDialog = true },
        )

        // 2. INFO BLOCKS: Scrollable content with back fields
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = MaterialTheme.spacing.mediumLarge,
                    end = MaterialTheme.spacing.mediumLarge,
                    bottom = MaterialTheme.spacing.mediumLarge,
                )
                .clip(RoundedCornerShape(12.dp)),
        ) {
            item {
                val coroutineScope = rememberCoroutineScope()
                val supportsAutoRefresh = remember(pkPassJson) {
                    pkPassJson?.webServiceURL != null
                }

                var isEnabled by remember(pass.autoRefreshEnabled, supportsAutoRefresh) {
                    mutableStateOf(if (supportsAutoRefresh) pass.autoRefreshEnabled else false)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    // Fixed white surface: intentional "paper card on coloured pass background"
                    // design. Theme surface colours must not be used here.
                    color = ColorTokens.pkPassBackSurface,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.mediumLarge),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.automatic_refresh),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (supportsAutoRefresh) {
                                    stringResource(R.string.auto_refresh_daily)
                                } else {
                                    stringResource(R.string.auto_refresh_unavailable)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { newValue ->
                                isEnabled = newValue
                                coroutineScope.launch {
                                    viewModel.setAutoRefreshEnabled(pass.id, newValue)
                                }
                            },
                            enabled = supportsAutoRefresh,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            // Check if there are back fields to show
            val hasBackFields = pkPassJson?.let { json ->
                val structure = json.boardingPass ?: json.eventTicket
                    ?: json.coupon ?: json.storeCard ?: json.generic
                structure?.backFields?.any { field ->
                    field.value?.toString()?.isNotBlank() == true
                } ?: false
            } ?: false

            if (hasBackFields) {
                pkPassJson.let { json ->
                    val structure = json.boardingPass ?: json.eventTicket
                        ?: json.coupon ?: json.storeCard ?: json.generic

                    structure?.backFields?.let { fields ->
                        items(fields) { field ->
                            val content = field.value?.toString() ?: ""
                            if (content.isNotBlank()) {
                                val localizedLabel = rememberLocalizedValue(field.label, pkPassJson)
                                val localizedValue = rememberLocalizedValue(content, pkPassJson)
                                InfoBlock(
                                    title = localizedLabel,
                                    htmlContent = localizedValue,
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                            }
                        }
                    }
                }
            } else {
                item {
                    EmptyStateMessage(
                        message = stringResource(R.string.no_additional_information),
                        tint = textColor,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    PassDeleteDialog(
        showDialog = showDeleteDialog,
        onDelete = {
            viewModel.deletePass(pass)
            showDeleteDialog = false
            onDismiss()
        },
        onDismiss = {
            showDeleteDialog = false
        },
    )
}
