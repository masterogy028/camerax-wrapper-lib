package com.dipl.cameraxlib.usecase.image_capture

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.CameraXController
import com.dipl.cameraxlib.OBImageCaptureException
import com.dipl.cameraxlib.createFile
import com.dipl.cameraxlib.getDefaultOutputDirectory
import com.dipl.cameraxlib.usecase.OBUseCase
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURED_CALLBACK
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_FLASH
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_MODE
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_SAVED_CALLBACK
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_LENS_FACING
import java.io.File

class OBImageCapture(private val parameters: ImageCaptureUseCaseParameters) : OBUseCase() {
    private val imageCaptureUseCase by lazy { useCase as ImageCapture }
    private lateinit var isCameraAvailable: () -> Boolean
    private lateinit var context: Context
    override fun build(pScreenAspectRatio: Int?, pRotation: Int?) {

        pScreenAspectRatio?.let { this.screenAspectRatio = it }
        pRotation?.let { this.rotation = it }

        ImageCapture.Builder()
            .setCaptureMode(parameters[OPTION_IMAGE_CAPTURE_MODE]!!)
            .setTargetAspectRatio(this.screenAspectRatio)
            .setTargetRotation(this.rotation)
            .setFlashMode(
                parameters[OPTION_IMAGE_CAPTURE_FLASH]!!
            )
            .build().also { useCase = it }
    }

    internal fun setCameraAvailabilityChecker(checkFun: () -> Boolean) {
        isCameraAvailable = checkFun
    }
    internal fun setContext(context: Context) {
        this.context = context
    }

    /**
     * Control function that executes [ImageCapture] use case if it is bound to the lifecycle.
     * Makes [ImageCapture.takePicture] call that captures a new image and saves it to a file along
     * with application specified metadata. Calls [ImageSavedCallback.onSuccess] if capture was
     * successful, else it makes [ImageSavedCallback.onError] call to a
     * [OPTION_IMAGE_SAVED_CALLBACK] preset parameter.
     *
     * @param context used for directory and file resolving
     * @param saveImageParams containing image file information
     *
     * @throws [OBImageCaptureException]
     */
    fun captureAndSaveImage(saveImageParams: SaveImageParams) {
        checkCameraAvailability()

        val imageSavedCallback = parameters[OPTION_IMAGE_SAVED_CALLBACK]
            ?: throw OBImageCaptureException("Parameter ImageSavedCallback is not set!")

        // Create output file to hold the image
        val photoFile = createFile(
            saveImageParams.outputDirectory ?: context.getDefaultOutputDirectory(),
            saveImageParams.fileName,
            saveImageParams.photoExtension
        )

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

        imageCaptureUseCase.takePicture(
            outputOptions,
            parameters[OPTION_IMAGE_CAPTURE_EXECUTOR]!!,
            object : ImageCapture.OnImageSavedCallback {
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
                        Log.d(
                            CameraXController.TAG,
                            "Image capture scanned into media store: $uri"
                        )
                        imageSavedCallback.onSuccess(uri)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    imageSavedCallback.onError(exception)
                }
            })
    }

    /**
     * Control function that executes [ImageCapture] use case if it is bound to the lifecycle.
     * Makes [ImageCapture.takePicture] call that captures a new still image for in memory access.
     * Calls [ImageCapturedCallback.onSuccess] if capture was successful, else it makes [ImageCapturedCallback.onError]
     * call to a [OPTION_IMAGE_CAPTURED_CALLBACK] preset parameter.
     *
     * @throws [OBImageCaptureException]
     */
    fun captureImage() {
        checkCameraAvailability()

        val imageCapturedCallback = parameters[OPTION_IMAGE_CAPTURED_CALLBACK]
            ?: throw OBImageCaptureException("Parameter ImageCapturedCallback is not set!")

        imageCaptureUseCase.takePicture(
            parameters[OPTION_IMAGE_CAPTURE_EXECUTOR]!!,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    imageCapturedCallback.onSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    imageCapturedCallback.onError(exception)
                }
            })
    }

    private fun checkCameraAvailability() {
        if (!this::isCameraAvailable.isInitialized || !isCameraAvailable()) {
            throw OBImageCaptureException("The camera is not available to this OBImageCapture use case.\nThe camera needs to be started by the controller.")
        }
    }
}

data class SaveImageParams(
    val fileName: String,
    val photoExtension: String,
    val outputDirectory: File? = null,
)
