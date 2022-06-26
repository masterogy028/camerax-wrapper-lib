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
     * The function is called every time the [lifecycleOwner] receives the ON_RESUME event.
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
    private var controllerState: InternalControllerState = InternalControllerState.CREATED

    /**
     * If the method is called after the controller creation the
     * [LifecycleEventObserver] will be registered. The observer will handle
     * [Lifecycle.Event.ON_RESUME], [Lifecycle.Event.ON_PAUSE] and
     * [Lifecycle.Event.ON_DESTROY] events so the user won't have to.
     *
     * Else, it only starts the camera with binding the use cases to the
     * lifecycleOwner by calling [updateCameraStateCallback] function.
     *
     * @param targetPreviewView
     * @param updateCameraStateCallback
     *
     * @throws OBDefaultException
     */
    fun start(targetPreviewView: PreviewView, updateCameraStateCallback: () -> Unit) {

        if (controllerState == InternalControllerState.CREATED) {
            // Registers the lifecycle observer
            lifecycleOwner.lifecycle.addObserver(
                CameraXLifecycleEventObserver(
                    targetPreviewView = targetPreviewView,
                    updateCameraStateCallback = updateCameraStateCallback,
                    stopCallback = { stop() },
                    closeCallback = { close() }
                )
            )
        } else {
            checkIsClosed("The camera controller is closed!")
            updateCameraStateCallback()
        }

        controllerState = InternalControllerState.STARTED
    }

    /**
     * Updates the internal [controllerState] accordingly.
     *
     * @throws OBDefaultException when the state is [InternalControllerState.CLOSED]
     */
    fun stop() {
        checkIsClosed("The camera controller is closed!")
        controllerState = InternalControllerState.STOPPED
    }

    /**
     * Updates the internal [controllerState] accordingly.
     *
     * @throws OBDefaultException when the state is [InternalControllerState.CLOSED]
     */
    private fun close() {
        checkIsClosed("The camera controller is already closed!")
        stop()
        controllerState = InternalControllerState.CLOSED
    }

    fun isCameraAvailable(hasSystemFeatureCheck: () -> Boolean): Boolean =
        (controllerState == InternalControllerState.STARTED) && hasSystemFeatureCheck()

    /**
     * @throws OBDefaultException when the state is [InternalControllerState.CLOSED]
     */
    private fun checkIsClosed(message: String) {
        if (controllerState == InternalControllerState.CLOSED)
            throw OBDefaultException(message)
    }

    /**
     * Enum class with values that represent the state of the controller.
     */
    private enum class InternalControllerState {
        CREATED,
        STARTED,
        STOPPED,
        CLOSED
    }

    /**
     * Class that handles [Lifecycle] events by calling provided callbacks.
     */
    private inner class CameraXLifecycleEventObserver(
        private val targetPreviewView: PreviewView,
        private val updateCameraStateCallback: () -> Unit,
        private val stopCallback: () -> Unit,
        private val closeCallback: () -> Unit
    ) : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                // we are posting the updateCameraStateCallback() call to the main
                // thread because targetPreviewView might not have been initialized yet.
                Lifecycle.Event.ON_RESUME -> targetPreviewView.post { updateCameraStateCallback() }
                Lifecycle.Event.ON_PAUSE -> stopCallback()
                Lifecycle.Event.ON_DESTROY -> closeCallback()
                else -> return
            }
        }
    }
}
