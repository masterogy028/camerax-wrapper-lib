package com.dipl.cameraxlib.usecase.preview

import android.util.DisplayMetrics
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_LENS_FACING
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_VIEW
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class OBPreview(private val parameters: PreviewUseCaseParameters) {
    lateinit var useCase: Preview
    private var screenAspectRatio = AspectRatio.RATIO_16_9
    private var rotation: Int = 0
    internal val lensFacing =
        parameters[OPTION_PREVIEW_LENS_FACING] ?: CameraSelector.LENS_FACING_BACK

    val cameraSelector =
        CameraSelector.Builder().requireLensFacing(lensFacing).build()

    fun build() {
        with(parameters[OPTION_PREVIEW_VIEW]!!) {
            val metrics = DisplayMetrics().also {
                display.getRealMetrics(it)
            }
            screenAspectRatio = clampAspectRatio(metrics.widthPixels, metrics.heightPixels)
            this@OBPreview.rotation = display.rotation
        }

        useCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
        with(parameters[OPTION_PREVIEW_VIEW]!!) {
            useCase.setSurfaceProvider(this.surfaceProvider)
            this.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    private fun clampAspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun getPreviewView(): View =
        parameters[OPTION_PREVIEW_VIEW]!!

    fun getAspectRation(): Int =
        screenAspectRatio

    fun getRotation(): Int =
        rotation

    companion object {

        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0

    }
}

inline fun createOBPreview(
    buildPreviewUseCaseParameters: PreviewUseCaseParameters.Builder.() -> Unit
): OBPreview {
    val builder = PreviewUseCaseParameters.Builder()
    builder.buildPreviewUseCaseParameters()
    return OBPreview(builder.build())
}
