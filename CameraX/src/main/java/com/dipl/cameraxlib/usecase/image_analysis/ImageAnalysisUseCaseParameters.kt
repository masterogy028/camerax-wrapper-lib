package com.dipl.cameraxlib.usecase.image_analysis

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import com.dipl.cameraxlib.CameraXExceptions
import com.dipl.cameraxlib.MissingMandatoryConfigParameterException
import com.dipl.cameraxlib.config.CameraXConfig
import com.dipl.cameraxlib.config.CameraXOption
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.ui.RectangleView
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_BACKPRESSURE_STRATEGY
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_CALLBACK
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_CROP_ANALYZE_AREA
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_SCANNER_TYPE
import com.dipl.cameraxlib.usecase.image_analysis.ImageCrop.WithRatio
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.BarcodeScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.FaceRecognitionScannerResultListener
import com.dipl.cameraxlib.usecase.image_analysis.analyzers.QRScannerResultListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AnalyzeUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: Config = AnalyzeImageUseCaseConfig()

        fun setCropAnalyzeArea(cropAnalyzeArea: ImageCrop): Builder {
            config.insertOption(OPTION_CROP_ANALYZE_AREA, cropAnalyzeArea)
            return this
        }

        fun setAnalyzeCallback(callback: AnalyzeImageListener): Builder {
            config.insertOption(OPTION_ANALYZE_CALLBACK, callback)
            return this
        }

        fun setScannerType(scannerType: OBScannerType): Builder {
            config.insertOption(OPTION_SCANNER_TYPE, scannerType)
            return this
        }

        fun setExecutorService(executor: ExecutorService): Builder {
            config.insertOption(OPTION_ANALYZE_EXECUTOR, executor)
            return this
        }

        fun setBackpressureStrategy(@ImageAnalysis.BackpressureStrategy backpressureStrategy: Int): Builder {
            config.insertOption(OPTION_ANALYZE_BACKPRESSURE_STRATEGY, backpressureStrategy)
            return this
        }

        fun build(): AnalyzeUseCaseParameters {
            config.mergeWithDefaults()
            return AnalyzeUseCaseParameters(config)
        }
    }

    companion object {
        // OPTIONS
        var OPTION_CROP_ANALYZE_AREA: Option<ImageCrop> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.cropArea",
                ImageCrop::class.java
            )

        var OPTION_ANALYZE_EXECUTOR: Option<ExecutorService> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.executor",
                ExecutorService::class.java
            )

        var OPTION_SCANNER_TYPE: Option<OBScannerType> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.scanner_type",
                OBScannerType::class.java
            )

        var OPTION_ANALYZE_CALLBACK: Option<AnalyzeImageListener> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.callback",
                AnalyzeImageListener::class.java
            )

        var OPTION_ANALYZE_BACKPRESSURE_STRATEGY: Option<Int> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.backpressure",
                Int::class.java
            )
    }
}

internal class AnalyzeImageUseCaseConfig : CameraXConfig() {

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
                val analyzeImageUseCaseConfig = AnalyzeImageUseCaseConfig()

                analyzeImageUseCaseConfig.insertOption(OPTION_CROP_ANALYZE_AREA, null)
                analyzeImageUseCaseConfig.insertOption(
                    OPTION_ANALYZE_EXECUTOR,
                    Executors.newSingleThreadExecutor()
                )
                analyzeImageUseCaseConfig.insertOption(OPTION_ANALYZE_CALLBACK, null)
                analyzeImageUseCaseConfig.insertOption(
                    OPTION_SCANNER_TYPE,
                    OBScannerType.DefaultScannerType
                )
                analyzeImageUseCaseConfig.insertOption(
                    OPTION_ANALYZE_BACKPRESSURE_STRATEGY,
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )

                return analyzeImageUseCaseConfig
            }
        }
    }
}

interface AnalyzeImageListener {
    fun analyze(image: Bitmap)
}

sealed class OBScannerType {
    object DefaultScannerType : OBScannerType()
    class BarcodeScannerType(val resultListener: BarcodeScannerResultListener) : OBScannerType()
    class QRScannerType(val resultListener: QRScannerResultListener) : OBScannerType()
    class FaceRecognitionScannerType(val resultListener: FaceRecognitionScannerResultListener) : OBScannerType()
}

/**
 * [aspectRatio] is ratio between height and width of the previewView.
 *
 * [heightRatio] is ratio between height of transparent rect and height of screen.
 *
 * [widthRatio] is ratio between width of transparent rect and width of screen.
 *
 * If you are using [RectangleView] or any other type of overlay
 * make sure that their height and width matches your [PreviewView]'s height and width
 * since you will be cropping a rectangle that lays over your [PreviewView].
 *
 * If you are using crop [WithRatio] make sure that you set the parameters properly.
 */
sealed class ImageCrop {
    abstract val aspectRatio: Float
    abstract val heightRatio: Float
    abstract val widthRatio: Float

    data class WithRectangleView(val view: RectangleView) : ImageCrop() {
        override val heightRatio: Float
            get() = view.heightRatio
        override val widthRatio: Float
            get() = view.widthRatio
        override val aspectRatio: Float
            get() = view.aspectRatio
    }

    data class WithRatio(val hRatio: Float, val wRatio: Float, val aRatio: Float) : ImageCrop() {
        override val widthRatio: Float
            get() = run { if (wRatio == 0f) throw CameraXExceptions.PictureAnalyzeException("You can not set the crop width ratio to 0.") else wRatio }
        override val heightRatio: Float
            get() = run { if (hRatio == 0f) throw CameraXExceptions.PictureAnalyzeException("You can not set the crop height ratio to 0.") else hRatio }
        override val aspectRatio: Float
            get() = run { if (aRatio == 0f) throw CameraXExceptions.PictureAnalyzeException("You can not set the crop aspect ratio to 0.") else aRatio }
    }
}

operator fun <T> AnalyzeUseCaseParameters.get(option: Option<T>): T? {
    return config.getOptionValue(option)
}
