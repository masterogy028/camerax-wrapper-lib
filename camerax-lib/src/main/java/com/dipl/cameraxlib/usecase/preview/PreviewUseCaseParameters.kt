package com.dipl.cameraxlib.usecase.preview

import androidx.camera.core.CameraSelector
import androidx.camera.core.impl.ImageOutputConfig
import androidx.camera.view.PreviewView
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.config.UseCaseConfig
import com.dipl.cameraxlib.config.UseCaseOption
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_LENS_FACING
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_VIEW
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_ROTATION

class PreviewUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: UseCaseConfig = PreviewUseCaseConfig()

        fun setLensFacing(@CameraSelector.LensFacing lensFacing: Int): Builder {
            config.insertOption(OPTION_PREVIEW_LENS_FACING, lensFacing)
            return this
        }

        fun setRotation(@ImageOutputConfig.RotationValue rotation: Int): Builder {
            config.insertOption(OPTION_ROTATION, rotation)
            return this
        }

        fun setPreviewView(previewView: PreviewView): Builder {
            config.insertOption(OPTION_PREVIEW_VIEW, previewView)
            return this
        }

        fun build(): PreviewUseCaseParameters {
            config.mergeWithDefaults()
            return PreviewUseCaseParameters(config)
        }
    }

    companion object {
        // OPTIONS
        var OPTION_PREVIEW_VIEW: Option<PreviewView> =
            UseCaseOption.create("ognjenbogicevic.preview.previewView", PreviewView::class.java)

        var OPTION_PREVIEW_LENS_FACING: Option<Int> =
            UseCaseOption.createNonMandatory("ognjenbogicevic.preview.lensFacing", Int::class.java)

        var OPTION_ROTATION: Option<Int> =
            UseCaseOption.createNonMandatory("ognjenbogicevic.preview.rotation", Int::class.java)

    }
}

internal class PreviewUseCaseConfig : UseCaseConfig() {

    override fun buildDefaultConfig(): Config {
        return Defaults.build()
    }

    private class Defaults {
        companion object {
            internal fun build(): Config {
                val defaultConfig = PreviewUseCaseConfig()

                defaultConfig.insertOption(OPTION_PREVIEW_VIEW, null)
                defaultConfig.insertOption(OPTION_ROTATION, null)
                defaultConfig.insertOption(
                    OPTION_PREVIEW_LENS_FACING,
                    CameraSelector.LENS_FACING_BACK
                )

                return defaultConfig
            }
        }
    }
}

operator fun <T> PreviewUseCaseParameters.get(option: Option<T>): T? {
    return config.getOptionValue(option)
}
