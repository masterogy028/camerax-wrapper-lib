package com.dipl.cameraxlib.usecase.image_capture

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.dipl.cameraxlib.CameraXController
import com.dipl.cameraxlib.CameraXExceptions
import com.dipl.cameraxlib.createFile
import com.dipl.cameraxlib.getDefaultOutputDirectory
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_CALLBACKS
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_FLASH
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_MODE
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_LENS_FACING
import java.io.File

class OBImageCapture(private val parameters: ImageCaptureUseCaseParameters) {
    lateinit var useCase: ImageCapture
    private var screenAspectRatio = AspectRatio.RATIO_16_9
    private var rotation: Int = 0
    private lateinit var isCameraAvailable: () -> Boolean

    fun build(screenAspectRatio: Int? = null, rotation: Int? = null) {

        screenAspectRatio?.let { this.screenAspectRatio = it }
        rotation?.let { this.rotation = it }

        ImageCapture.Builder()
            .setCaptureMode(parameters[OPTION_IMAGE_CAPTURE_MODE]!!)
            .setTargetAspectRatio(this.screenAspectRatio)
            .setTargetRotation(this.rotation)
            .setFlashMode(
                parameters[OPTION_IMAGE_CAPTURE_FLASH]!!
            )
            .build().also { useCase = it }
    }

    fun setCameraAvailabilityChecker(checkFun: () -> Boolean) {
        isCameraAvailable = checkFun
    }

    /**
     * Control function that executes [ImageCapture] use case if it is bound to the lifecycle.
     *
     * @throws [CameraXExceptions.ImageCaptureException]
     */
    fun takePicture(
        fileName: String,
        photoExtension: String,
        context: Context,
        outputDirectory: File = context.getDefaultOutputDirectory(),
    ) {
        if (!this::isCameraAvailable.isInitialized || !isCameraAvailable()) {
            throw CameraXExceptions.ImageCaptureException("The camera is not available to this OBImageCapture use case.\nThe camera needs to be started by the controller.")
        }

        // Create output file to hold the image
        val photoFile = createFile(outputDirectory, fileName, photoExtension)

        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {

            // Mirror image when using the front camera
            isReversedHorizontal =
                parameters[OPTION_PREVIEW_LENS_FACING] == CameraSelector.LENS_FACING_FRONT
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        // Setup image capture listener which is triggered after photo has been taken
        useCase.takePicture(
            outputOptions,
            parameters[OPTION_IMAGE_CAPTURE_EXECUTOR]!!,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(CameraXController.TAG, "Photo capture failed: ${exc.message}", exc)
                    parameters[OPTION_IMAGE_CAPTURE_CALLBACKS]!!
                        .onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Log.d(CameraXController.TAG, "Photo capture succeeded: $savedUri")

                    // If the folder selected is an external media directory, this is
                    // unnecessary but otherwise other apps will not be able to access our
                    // images unless we scan them using [MediaScannerConnection]
                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(File(savedUri.path).extension)
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(File(savedUri.path).absolutePath),
                        arrayOf(mimeType)
                    ) { _, uri ->
                        Log.d(CameraXController.TAG, "Image capture scanned into media store: $uri")
                        parameters[OPTION_IMAGE_CAPTURE_CALLBACKS]!!
                            .onSuccess(uri)
                    }
                }
            })
    }
}

inline fun createOBImageCapture(
    buildImageCaptureUseCaseParameters: ImageCaptureUseCaseParameters.Builder.() -> Unit
): OBImageCapture {
    val builder = ImageCaptureUseCaseParameters.Builder()
    builder.buildImageCaptureUseCaseParameters()
    return OBImageCapture(builder.build())
}
