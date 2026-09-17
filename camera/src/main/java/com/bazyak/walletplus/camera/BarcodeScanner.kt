package com.bazyak.walletplus.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * ML Kit barcode scanner wrapper.
 * Handles both camera-based scanning and image-based scanning.
 */
class BarcodeScanner(private val context: Context) {

    private val scanner = BarcodeScanning.getClient()

    /**
     * Analyzes a camera frame for barcodes.
     * Used with CameraX ImageAnalysis use case.
     */
    @androidx.camera.core.ExperimentalGetImage
    fun analyzeCameraImage(
        imageProxy: ImageProxy,
        onBarcodeDetected: (ScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    val scanResult = processBarcode(barcode)
                    if (scanResult != null) {
                        onBarcodeDetected(scanResult)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeScanner", "Camera scanning failed", e)
                onError("Scanning failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Analyzes an image from gallery for barcodes.
     */
    suspend fun analyzeImageFromUri(
        uri: Uri,
        onBarcodeDetected: (ScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val inputImage = InputImage.fromFilePath(context, uri)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isEmpty()) {
                        onError("No barcode found in image")
                    } else {
                        val barcode = barcodes.first()
                        val scanResult = processBarcode(barcode)
                        if (scanResult != null) {
                            onBarcodeDetected(scanResult)
                        } else {
                            onError("Could not read barcode from image")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeScanner", "Image scanning failed", e)
                    onError("Scanning failed: ${e.message}")
                }
                .await()
        } catch (e: IOException) {
            Log.e("BarcodeScanner", "Failed to load image", e)
            onError("Failed to load image")
        }
    }

    /**
     * Processes a detected barcode and classifies it as URL or regular barcode.
     */
    private fun processBarcode(barcode: Barcode): ScanResult? {
        val rawValue = barcode.rawValue ?: return null

        // Check if it's a URL
        return if (rawValue.startsWith("http://", ignoreCase = true) ||
            rawValue.startsWith("https://", ignoreCase = true)
        ) {
            ScanResult.UrlDetected(rawValue)
        } else {
            // Regular barcode (loyalty card code)
            ScanResult.BarcodeDetected(
                value = rawValue,
                format = barcode.format
            )
        }
    }

    /**
     * Creates an ImageAnalysis.Analyzer for use with CameraX.
     */
    @androidx.camera.core.ExperimentalGetImage
    fun createAnalyzer(
        onBarcodeDetected: (ScanResult) -> Unit,
        onError: (String) -> Unit
    ): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { imageProxy ->
            analyzeCameraImage(imageProxy, onBarcodeDetected, onError)
        }
    }

    /**
     * Release resources.
     */
    fun close() {
        scanner.close()
    }
}
