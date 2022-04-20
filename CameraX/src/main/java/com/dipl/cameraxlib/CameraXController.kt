package com.dipl.cameraxlib

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dipl.cameraxlib.usecase.image_analysis.OBImageAnalysis
import com.dipl.cameraxlib.usecase.image_capture.OBImageCapture
import com.dipl.cameraxlib.usecase.preview.OBPreview

abstract class CameraXController protected constructor(
    protected val context: Context,
    protected val lifecycleOwner: LifecycleOwner,
    protected val obPreview: OBPreview,
    protected val obImageAnalysis: OBImageAnalysis? = null,
    protected val obImageCapture: OBImageCapture? = null
) {

    private var cameraState: CameraXState = CameraXState.CREATED

    protected var camera: Camera? = null
    protected var cameraProvider: ProcessCameraProvider? = null

    /**
     * The function initializes cameraX with selected use cases.
     *
     * The function should be called every time the configuration changes.
     *
     * @throws [CameraXExceptions.DefaultException] in case of binding failure.
     */
    protected abstract fun updateCameraState()

    /**
     * The function checks is the camera is available for action.
     *
     * @return true is the camera is started; false otherwise.
     */
    open fun isCameraAvailable(hardwareCameraFeature: String): Boolean =
        (cameraState == CameraXState.STARTED) &&
                context.packageManager.hasSystemFeature(hardwareCameraFeature)

    /**
     * If the start method is called after the controller creation the
     * observer will be registered else it starts the camera with binding
     * the use cases to the lifecycleOwner.
     */
    fun start() {

        // Registers the lifecycle observer.
        if (cameraState == CameraXState.CREATED) {
            Log.d(TAG, "bindObserver: //////")

            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        // If the preview use case is used then we post the call to updateCameraState()
                        // else we just call updateCameraState() because there is no waiting for UI components to be initialized
                        Lifecycle.Event.ON_RESUME -> if (obPreview?.getPreviewView()
                                ?.post { start() } == null
                        )
                            start()
                        Lifecycle.Event.ON_PAUSE -> stop()
                        Lifecycle.Event.ON_DESTROY -> close()
                        else -> return
                    }
                }
            })
        } else {
            if (cameraState == CameraXState.CLOSED) {
                throw OBDefaultException(
                    "The camera controller is closed!" +
                            "\nYou might be using the same controller in multiple fragments/activities."
                )
            }
            Log.d(TAG, "start: //////")
            updateCameraState()
        }

        cameraState = CameraXState.STARTED
    }

    /**
     * Stops the camera with unbinding currently bound use cases and sets the properties to null
     * as they are not going to be used again before calling [start].
     */
    open fun stop() {
        Log.d(TAG, "stop: //////")
        if (cameraState == CameraXState.CLOSED) {
            throw OBDefaultException(
                "The camera controller is closed!" +
                        "\nYou might be using the same controller in multiple fragments/activities."
            )
        }
        cameraProvider?.unbindAll()
        cameraProvider = null
        camera = null
        cameraState = CameraXState.STOPPED
    }

    /**
     * Control function that stops the controller and sets the [cameraState] to CameraXState.CLOSED,
     * preventing further controller actions.
     */
    protected open fun close() {
        Log.d(TAG, "close: //////")
        if (cameraState == CameraXState.CLOSED) {
            throw OBDefaultException("The camera controller is already closed!")
        }
        stop()
        cameraState = CameraXState.CLOSED
    }

    companion object {

        const val TAG = "CameraXController"

        /**
         * Gets the instance for @param[useCaseParameters].
         *
         * Depending on the parameters, the appropriate controller will be instantiated.
         *
         * @return [CameraXController] instance.
         */
        fun getControllerForParameters(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            obPreview: OBPreview,
            obImageAnalysis: OBImageAnalysis? = null,
            obImageCapture: OBImageCapture? = null
        ): CameraXController =
            DefaultCameraXController.createController(
                context,
                lifecycleOwner,
                obPreview,
                obImageAnalysis,
                obImageCapture
            )
    }

    /**
     * Enum class with values that represent the state of the controller.
     */
    protected enum class CameraXState {
        CREATED,
        STARTED,
        STOPPED,
        CLOSED
    }
}
