package com.dipl.cameraxlib.usecase.image_analysis

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import com.dipl.cameraxlib.MissingMandatoryConfigParameterException
import com.dipl.cameraxlib.config.CameraXOption
import com.dipl.cameraxlib.config.CameraXUseCaseConfig
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_BACKPRESSURE_STRATEGY
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_CALLBACK
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_ANALYZE_INTERVAL
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_CROP_ANALYZE_AREA
import com.dipl.cameraxlib.usecase.image_analysis.AnalyzeUseCaseParameters.Companion.OPTION_SCANNER_TYPE
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop
import com.dipl.cameraxlib.usecase.image_analysis.models.OBScannerType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AnalyzeUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: Config = AnalyzeImageUseCaseConfig()

        fun setCropAnalyzeArea(cropAnalyzeArea: ImageCrop): Builder {
            config.insertOption(OPTION_CROP_ANALYZE_AREA, cropAnalyzeArea)
            return this
        }

        fun setAnalyzeInterval(analyzeInterval: Long): Builder {
            config.insertOption(OPTION_ANALYZE_INTERVAL, analyzeInterval)
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

        var OPTION_ANALYZE_INTERVAL: Option<Long> =
            CameraXOption.createNonMandatory(
                "ognjenbogicevic.analyze.interval",
                Long::class.java
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

internal class AnalyzeImageUseCaseConfig : CameraXUseCaseConfig() {

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
                analyzeImageUseCaseConfig.insertOption(OPTION_ANALYZE_INTERVAL, null)
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

operator fun <T> AnalyzeUseCaseParameters.get(option: Option<T>): T? {
    return config.getOptionValue(option)
}
