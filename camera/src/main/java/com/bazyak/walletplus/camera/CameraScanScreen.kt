package com.bazyak.walletplus.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bazyak.walletplus.designsystem.components.branding.AppLogo
import com.bazyak.walletplus.designsystem.components.button.WalletIconButton
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Camera scanning screen with ML Kit barcode detection.
 * Supports both camera scanning and gallery image scanning.
 * Full screen design with edge-to-edge camera view.
 */
@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraScanScreen(
    onScanResult: (ScanResult) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanner(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            coroutineScope.launch {
                barcodeScanner.analyzeImageFromUri(
                    uri = uri,
                    onBarcodeDetected = { scanResult ->
                        if (!hasScanned) {
                            hasScanned = true
                            isProcessing = false
                            onScanResult(scanResult)
                        }
                    },
                    onError = { message ->
                        isProcessing = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Full screen camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()

                        // Preview use case
                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        // Image analysis use case for barcode scanning
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    cameraExecutor,
                                    barcodeScanner.createAnalyzer(
                                        onBarcodeDetected = { scanResult ->
                                            if (!hasScanned) {
                                                hasScanned = true
                                                onScanResult(scanResult)
                                            }
                                        },
                                        onError = { message ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    )
                                )
                            }

                        // Select back camera
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Camera initialization failed")
                            }
                        }
                    },
                    ContextCompat.getMainExecutor(ctx)
                )

                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Top center: App logo
        AppLogo(
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
        )

        // Top left: Close button
        TintedButton(
            icon = com.bazyak.walletplus.designsystem.R.drawable.cross,
            contentDescription = "Close",
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopStart),
        )

        // Top right: Gallery button
        TintedButton(
            icon = com.bazyak.walletplus.designsystem.R.drawable.image,
            contentDescription = "Pick from gallery",
            onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                    ),
                )
            },
            modifier = Modifier.align(Alignment.TopEnd),
        )

        // Center: Scanning frame
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Bottom: Instructions card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = "Point at QR code or barcode",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }

        // Snackbar host
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
        ) {
            SnackbarHost(snackbarHostState)
        }

        // Loading indicator
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TintedButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WalletIconButton(
        onClick = onClick,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 48.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }
}
