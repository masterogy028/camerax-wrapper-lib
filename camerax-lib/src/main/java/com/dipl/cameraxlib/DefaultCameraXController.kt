package com.dipl.cameraxlib

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dipl.cameraxlib.usecase.image_analysis.OBImageAnalysis
import com.dipl.cameraxlib.usecase.image_capture.OBImageCapture
import com.dipl.cameraxlib.usecase.preview.OBPreview

class DefaultCameraXController private constructor(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    obPreview: OBPreview,
    obImageAnalysis: OBImageAnalysis? = null,
    obImageCapture: OBImageCapture? = null
) :
    CameraXController(context, lifecycleOwner, obPreview, obImageAnalysis, obImageCapture) {

    override fun updateCameraState() {

        // building obPreview here, after we are sure that the ui elements are rendered
        obPreview.build()

        // ImageAnalysis and ImageCapture use cases are optional
        obImageAnalysis?.build(obPreview.getAspectRatio(), obPreview.getCurrentRotation())
        obImageCapture?.build(obPreview.getAspectRatio(), obPreview.getCurrentRotation())

        // setting the function that checks camera's availability
        obImageCapture?.setCameraAvailabilityChecker { isCameraAvailable(obPreview.lensFacing.toPackageManagerCameraFeature()) }

        val cameraSelector = obPreview.cameraSelector

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            cameraProvider = cameraProviderFuture.get()
            cameraProvider?.unbindAll()

            val useCases = arrayOf(
                obPreview.useCase,
                obImageAnalysis?.useCase,
                obImageCapture?.useCase
            ).filterNotNull()

            try {
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray()
                )
            } catch (exc: Exception) {
                throw OBDefaultException("Use case binding failed! Check the validity of the use case parameters!", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    companion object {

        /**
         * Gets [DefaultCameraXController] instance for the required parameters.
         *
         * @return [CameraXController]
         */
        fun create(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            obPreview: OBPreview,
            obImageAnalysis: OBImageAnalysis? = null,
            obImageCapture: OBImageCapture? = null
        ): CameraXController =
            DefaultCameraXController(
                context,
                lifecycleOwner,
                obPreview,
                obImageAnalysis,
                obImageCapture
            )
    }
}

private fun Int.toPackageManagerCameraFeature(): String =
    when (this) {
        CameraSelector.LENS_FACING_BACK -> PackageManager.FEATURE_CAMERA_ANY
        else -> PackageManager.FEATURE_CAMERA_FRONT
    }
