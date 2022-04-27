package com.dipl.cameraxlib.usecase.image_analysis.models

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeImageListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.*

sealed class OBScannerType {
    object DefaultScannerType : OBScannerType()
    class BarcodeScannerType(val resultListener: BarcodeScannerResultListener) : OBScannerType()
    class QRScannerType(val resultListener: QRScannerResultListener) : OBScannerType()
    class FaceRecognitionScannerType(val resultListener: FaceRecognitionScannerResultListener) :
        OBScannerType()
}

fun OBScannerType.getAnalyzer(
    imageCrop: ImageCrop?,
    analyzeImageListener: AnalyzeImageListener?,
    analyzeInterval: Long?
): ImageAnalysis.Analyzer {
    val analyzeCallback = { image: Bitmap ->
        analyzeImageListener?.analyze(image) ?: Unit
    }
    return when (val scanType = this) {
        is OBScannerType.DefaultScannerType -> {
            DefaultImageAnalyzer(
                imageCrop,
                analyzeCallback,
                analyzeInterval
            )
        }
        is OBScannerType.QRScannerType -> {
            QRScanner(
                scanType.resultListener,
                imageCrop,
                analyzeCallback,
                analyzeInterval
            )
        }
        is OBScannerType.BarcodeScannerType -> {
            BarcodeScanner(
                scanType.resultListener,
                imageCrop,
                analyzeCallback,
                analyzeInterval
            )
        }
        is OBScannerType.FaceRecognitionScannerType -> {
            FaceRecognitionScanner(
                scanType.resultListener,
                imageCrop,
                analyzeCallback,
                analyzeInterval
            )
        }
    }
}
