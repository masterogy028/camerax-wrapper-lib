package com.dipl.cameraxlib.usecase.image_analysis.analyzers

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceRecognitionScanner(
    private val registeredResultListener: FaceRecognitionScannerResultListener,
    imageCrop: ImageCrop?,
    analyzeImageBitmap: AnalyzeImageLambda = {},
    analyzeInterval: Long?
) : DefaultImageAnalyzer(imageCrop, analyzeImageBitmap, analyzeInterval) {

    // High-accuracy landmark detection and face classification
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // Real-time contour detection
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    override fun analyze(image: ImageProxy) {
        super.analyze(image)
        lastAnalyzedFrame?.also { bitmapImage ->
            val inputImage = InputImage.fromBitmap(bitmapImage, 0)
            detector.process(inputImage).addOnSuccessListener { faces ->
                registeredResultListener.onSuccessResult(faces, bitmapImage)
            }
                .addOnFailureListener { registeredResultListener.onFailure(it) }
        }
    }
}

interface FaceRecognitionScannerResultListener {
    fun onSuccessResult(faces: MutableList<Face>, originalBitmap: Bitmap) {}
    fun onFailure(e: Exception) {}
}
