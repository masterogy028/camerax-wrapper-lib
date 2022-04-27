package com.dipl.cameraxlib.usecase.image_analysis

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_BACKPRESSURE_STRATEGY
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_CALLBACK
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_INTERVAL
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_CROP_ANALYZE_AREA
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_SCANNER_TYPE
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.BarcodeScanner
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.DefaultImageAnalyzer
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.FaceRecognitionScanner
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.QRScanner
import com.dipl.cameraxlib.usecase.image_analysis.models.OBScannerType
import com.dipl.cameraxlib.usecase.image_analysis.models.getAnalyzer

class OBImageAnalysis(private val parameters: AnalyzeUseCaseParameters) {

    lateinit var useCase: ImageAnalysis
    private var screenAspectRatio: Int = AspectRatio.RATIO_16_9
    private var rotation: Int = 0

    private val imageAnalyzer = parameters[OPTION_SCANNER_TYPE]!!.getAnalyzer(
        imageCrop = parameters[OPTION_CROP_ANALYZE_AREA],
        analyzeImageListener = parameters[OPTION_ANALYZE_CALLBACK],
        analyzeInterval = parameters[OPTION_ANALYZE_INTERVAL]
    )

    fun build(screenAspectRatio: Int? = null, rotation: Int? = null) {

        screenAspectRatio?.let { this.screenAspectRatio = it }
        rotation?.let { this.rotation = it }

        useCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(this.screenAspectRatio)
            .setTargetRotation(this.rotation)
            .setBackpressureStrategy(parameters[OPTION_ANALYZE_BACKPRESSURE_STRATEGY]!!)
            .build()
            .also {
                it.setAnalyzer(
                    parameters[OPTION_ANALYZE_EXECUTOR]!!,
                    imageAnalyzer
                )
            }
    }
}

inline fun createOBImageAnalysis(
    buildImageAnalysisUseCaseParameters: AnalyzeUseCaseParameters.Builder.() -> Unit
): OBImageAnalysis {
    val builder = AnalyzeUseCaseParameters.Builder()
    builder.buildImageAnalysisUseCaseParameters()
    return OBImageAnalysis(builder.build())
}
