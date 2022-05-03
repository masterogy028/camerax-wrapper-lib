package com.dipl.cameraxlib

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
    private val lifecycleAwareController: LifecycleAwareController =
        LifecycleAwareController(lifecycleOwner)

    protected var cameraProvider: ProcessCameraProvider? = null

    /**
     * Control function that starts camerax use cases.
     */
    fun start() {
        lifecycleAwareController.start(
            targetPreviewView = obPreview.getPreviewView(),
            updateCameraStateCallback = { updateCameraState() }
        )
    }

    /**
     * Stops camerax use cases by unbinding them.
     */
    fun stop() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        lifecycleAwareController.stop()
    }

    /**
     * The function initializes camerax with selected use cases.
     *
     * The function should be called every time the device configuration changes.
     *
     * @throws [OBDefaultException] in case of binding failure.
     */
    protected abstract fun updateCameraState()

    /**
     * The function checks is the camera is available for action.
     *
     * @return true is the camera is started; false otherwise.
     */
    internal fun isCameraAvailable(hardwareCameraFeature: String): Boolean =
        lifecycleAwareController.isCameraAvailable {
            context.packageManager.hasSystemFeature(
                hardwareCameraFeature
            )
        }

    companion object {

        const val TAG = "CameraXController"

        /**
         * Function that will instantiate the appropriate controller depending on the provided parameters.
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
            DefaultCameraXController.create(
                context,
                lifecycleOwner,
                obPreview,
                obImageAnalysis,
                obImageCapture
            )
    }
}

internal class LifecycleAwareController(
    private val lifecycleOwner: LifecycleOwner,
) {
    private var cameraState: InternalCameraState = InternalCameraState.CREATED

    /**
     * If the method is called after the controller creation the
     * [LifecycleEventObserver] will be registered. The observer will handle
     * [Lifecycle.Event.ON_RESUME], [Lifecycle.Event.ON_PAUSE] and
     * [Lifecycle.Event.ON_DESTROY] events so user won't have to.
     *
     * Else, it starts the camera with binding the use cases to the
     * lifecycleOwner by calling [updateCameraStateCallback] function.
     *
     * @param targetPreviewView
     * @param updateCameraStateCallback
     *
     * @throws OBDefaultException
     */
    fun start(targetPreviewView: PreviewView, updateCameraStateCallback: () -> Unit) {

        if (cameraState == InternalCameraState.CREATED) {
            // Registers the lifecycle observer
            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        // If the preview use case is used then we post the call to updateCameraState()
                        // else we just call updateCameraState() because there is no waiting for UI components to be initialized
                        Lifecycle.Event.ON_RESUME -> targetPreviewView.post { updateCameraStateCallback() }
                        Lifecycle.Event.ON_PAUSE -> stop()
                        Lifecycle.Event.ON_DESTROY -> close()
                        else -> return
                    }
                }
            })
        } else {
            checkIsClosed(
                "The camera controller is closed!" +
                        "\nYou might be using the same controller in multiple fragments/activities."
            )
            updateCameraStateCallback()
        }

        cameraState = InternalCameraState.STARTED
    }

    /**
     * Updates the internal [cameraState] accordingly.
     *
     * @throws OBDefaultException when the state is [InternalCameraState.CLOSED]
     */
    fun stop() {
        checkIsClosed(
            "The camera controller is closed!" +
                    "\nYou might be using the same controller in multiple fragments/activities."
        )
        if (cameraState == InternalCameraState.CLOSED) {
            throw OBDefaultException(
                "The camera controller is closed!" +
                        "\nYou might be using the same controller in multiple fragments/activities."
            )
        }
        cameraState = InternalCameraState.STOPPED
    }

    /**
     * Updates the internal [cameraState] accordingly.
     *
     * @throws OBDefaultException when the state is [InternalCameraState.CLOSED]
     */
    fun close() {
        checkIsClosed("The camera controller is already closed!")
        stop()
        cameraState = InternalCameraState.CLOSED
    }

    fun isCameraAvailable(hasSystemFeatureCheck: () -> Boolean): Boolean =
        (cameraState == InternalCameraState.STARTED) && hasSystemFeatureCheck()

    /**
     * @throws OBDefaultException when the state is [InternalCameraState.CLOSED]
     */
    private fun checkIsClosed(message: String) {
        if (cameraState == InternalCameraState.CLOSED)
            throw OBDefaultException(message)
    }

    /**
     * Enum class with values that represent the state of the controller.
     */
    enum class InternalCameraState {
        CREATED,
        STARTED,
        STOPPED,
        CLOSED
    }
}
