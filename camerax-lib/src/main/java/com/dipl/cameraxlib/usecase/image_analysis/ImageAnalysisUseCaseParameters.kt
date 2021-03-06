package com.dipl.cameraxlib.usecase.image_analysis

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import com.dipl.cameraxlib.MissingMandatoryConfigParameterException
import com.dipl.cameraxlib.config.UseCaseOption
import com.dipl.cameraxlib.config.UseCaseConfig
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_ANALYZE_BACKPRESSURE_STRATEGY
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_ANALYZE_CALLBACK
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_ANALYZE_EXECUTOR
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_ANALYZE_INTERVAL
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_CROP_ANALYZE_AREA
import com.dipl.cameraxlib.usecase.image_analysis.ImageAnalysisUseCaseParameters.Companion.OPTION_SCANNER_TYPE
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop
import com.dipl.cameraxlib.usecase.image_analysis.models.OBScannerType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageAnalysisUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: UseCaseConfig = ImageAnalysisUseCaseConfig()

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

        fun build(): ImageAnalysisUseCaseParameters {
            config.mergeWithDefaults()
            return ImageAnalysisUseCaseParameters(config)
        }
    }

    companion object {
        // OPTIONS
        var OPTION_CROP_ANALYZE_AREA: Option<ImageCrop> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.cropArea",
                ImageCrop::class.java
            )

        var OPTION_ANALYZE_INTERVAL: Option<Long> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.interval",
                Long::class.java
            )

        var OPTION_ANALYZE_EXECUTOR: Option<ExecutorService> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.executor",
                ExecutorService::class.java
            )

        var OPTION_SCANNER_TYPE: Option<OBScannerType> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.scanner_type",
                OBScannerType::class.java
            )

        var OPTION_ANALYZE_CALLBACK: Option<AnalyzeImageListener> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.callback",
                AnalyzeImageListener::class.java
            )

        var OPTION_ANALYZE_BACKPRESSURE_STRATEGY: Option<Int> =
            UseCaseOption.createNonMandatory(
                "ognjenbogicevic.analyze.backpressure",
                Int::class.java
            )
    }
}

internal class ImageAnalysisUseCaseConfig : UseCaseConfig() {

    override fun buildDefaultConfig(): Config {
        return Defaults.build()
    }

    private class Defaults {
        companion object {
            internal fun build(): Config {
                val analyzeImageUseCaseConfig = ImageAnalysisUseCaseConfig()

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

operator fun <T> ImageAnalysisUseCaseParameters.get(option: Option<T>): T? {
    return config.getOptionValue(option)
}
