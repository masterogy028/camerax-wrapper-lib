package com.dipl.cameraxlib.usecase.image_analysis.analyzers

import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeScanner(
    private val registeredResultListener: BarcodeScannerResultListener,
    imageCrop: ImageCrop?,
    analyzeImageBitmap: AnalyzeImageLambda = {},
    analyzeInterval: Long?
) : DefaultImageAnalyzer(imageCrop, analyzeImageBitmap, analyzeInterval) {

    private val scannerOptions = BarcodeScannerOptions.Builder().build()
    private val scanner = BarcodeScanning.getClient(scannerOptions)

    override fun analyze(image: ImageProxy) {
        super.analyze(image)
        lastAnalyzedFrame?.also { bitmapImage ->
            val inputImage = InputImage.fromBitmap(bitmapImage, 0)
            scanner.process(inputImage).addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.apply {
                    registeredResultListener.onSuccessStringResult(displayValue ?: "")
                    registeredResultListener.onSuccessResult(this)
                }
            }
                .addOnFailureListener { registeredResultListener.onFailure(it) }
        }
    }
}

interface BarcodeScannerResultListener {
    fun onSuccessStringResult(result: String) {}
    fun onSuccessResult(barcode: Barcode?) {}
    fun onFailure(e: Exception) {}
}
