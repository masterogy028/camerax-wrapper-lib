package com.dipl.cameraxlib.usecase.image_capture

import android.net.Uri
import androidx.camera.core.ImageCapture
import com.dipl.cameraxlib.MissingMandatoryConfigParameterException
import com.dipl.cameraxlib.config.CameraXUseCaseConfig
import com.dipl.cameraxlib.config.CameraXOption
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_CALLBACKS
import com.dipl.cameraxlib.usecase.image_capture.ImageCaptureUseCaseParameters.Companion.OPTION_IMAGE_CAPTURE_EXECUTOR
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageCaptureUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: Config = ImageCaptureUseCaseConfig()

        fun setExecutor(executor: ExecutorService): Builder {
            config.insertOption(OPTION_IMAGE_CAPTURE_EXECUTOR, executor)
            return this
        }

        fun setImageCaptureCallback(callback: ImageCaptureCallbacks): Builder {
            config.insertOption(OPTION_IMAGE_CAPTURE_CALLBACKS, callback)
            return this
        }

        fun setFlashMode(@ImageCapture.FlashMode mode: Int): Builder {
            config.insertOption(OPTION_IMAGE_CAPTURE_FLASH, mode)
            return this
        }

        fun setCaptureMode(@ImageCapture.CaptureMode mode: Int): Builder {
            config.insertOption(OPTION_IMAGE_CAPTURE_MODE, mode)
            return this
        }

        fun build(): ImageCaptureUseCaseParameters {
            config.mergeWithDefaults()
            return ImageCaptureUseCaseParameters(config)
        }
    }

    companion object {
        // OPTIONS
        var OPTION_IMAGE_CAPTURE_EXECUTOR: Option<ExecutorService> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.imageCapture.executor",
                ExecutorService::class.java
            )

        var OPTION_IMAGE_CAPTURE_CALLBACKS: Option<ImageCaptureCallbacks> =
            CameraXOption.create(
                "ognjenbogicevic.imageCapture.callback",
                ImageCaptureCallbacks::class.java
            )

        var OPTION_IMAGE_CAPTURE_MODE: Option<Int> =
            CameraXOption.createNonMandatory("ognjenbogicevic.imageCapture.captureMode", Int::class.java)

        var OPTION_IMAGE_CAPTURE_FLASH: Option<Int> =
            CameraXOption.createNonMandatory("ognjenbogicevic.imageCapture.flashMode", Int::class.java)
    }
}

internal class ImageCaptureUseCaseConfig : CameraXUseCaseConfig() {

    override fun buildDefaultConfig(): Config {
        return Defaults.build()
    }

    override fun mergeWithDefaults() {
        val default: Config = buildDefaultConfig()
        for (option: Option<*> in default.listOptions()) {
            if (option.isMandatory() && !this.containsOption(option)) {
                throw MissingMandatoryConfigParameterException(option)
            }
            insertOption(option, getOptionValue(option) ?: default.getOptionValue(option))
        }
    }

    private class Defaults {
        companion object {
            internal fun build(): Config {
                val defaultConfig = ImageCaptureUseCaseConfig()

                defaultConfig.insertOption(
                    OPTION_IMAGE_CAPTURE_EXECUTOR,
                    Executors.newSingleThreadExecutor()
                )
                defaultConfig.insertOption(OPTION_IMAGE_CAPTURE_CALLBACKS, null)
                defaultConfig.insertOption(
                    ImageCaptureUseCaseParameters.OPTION_IMAGE_CAPTURE_FLASH,
                    ImageCapture.FLASH_MODE_OFF
                )
                defaultConfig.insertOption(
                    ImageCaptureUseCaseParameters.OPTION_IMAGE_CAPTURE_MODE,
                    ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                )

                return defaultConfig
            }
        }
    }
}

interface ImageCaptureCallbacks {
    fun onSuccess(uri: Uri)
    fun onError(e: Exception)
}

operator fun <T> ImageCaptureUseCaseParameters.get(option: Option<T>): T? {
    return config.getOptionValue(option)
}
