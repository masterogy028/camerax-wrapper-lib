package com.dipl.cameraxlib.dsl_builders

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.dipl.cameraxlib.CameraXController
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters
import com.dipl.cameraxlib.usecase.image_analysis.OBImageAnalysis
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters
import com.dipl.cameraxlib.usecase.image_capture.OBImageCapture
import com.dipl.cameraxlib.usecase.preview.OBPreview
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters

class CameraXControllerDSLBuilder {
    internal lateinit var preview: OBPreview
    internal var imageCapture: OBImageCapture? = null
    internal var imageAnalysis: OBImageAnalysis? = null

    fun preview(build: PreviewUseCaseParameters.Builder.() -> Unit) {
        val builder = PreviewUseCaseParameters.Builder()
        preview = OBPreview(builder.apply(build).build())
    }

    fun imageAnalysis(build: ImageAnalysisUseCaseParameters.Builder.() -> Unit) {
        val builder = ImageAnalysisUseCaseParameters.Builder()
        imageAnalysis = OBImageAnalysis(builder.apply(build).build())
    }

    fun imageCapture(build: ImageCaptureUseCaseParameters.Builder.() -> Unit): OBImageCapture {
        val builder = ImageCaptureUseCaseParameters.Builder()
        imageCapture = OBImageCapture(builder.apply(build).build())
        return imageCapture!!
    }
}

fun cameraXController(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    build: CameraXControllerDSLBuilder.() -> Unit
): CameraXController =
    with(CameraXControllerDSLBuilder().apply(build)) {
        CameraXController.getControllerForParameters(
            context,
            lifecycleOwner,
            preview,
            imageAnalysis,
            imageCapture
        )
    }
