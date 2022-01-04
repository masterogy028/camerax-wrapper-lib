package com.dipl.cameraxlib.usecase.preview

import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import com.dipl.cameraxlib.MissingMandatoryConfigParameterException
import com.dipl.cameraxlib.config.CameraXUseCaseConfig
import com.dipl.cameraxlib.config.CameraXOption
import com.dipl.cameraxlib.config.Config
import com.dipl.cameraxlib.config.Option
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_LENS_FACING
import com.dipl.cameraxlib.usecase.preview.PreviewUseCaseParameters.Companion.OPTION_PREVIEW_VIEW

class PreviewUseCaseParameters private constructor(val config: Config) {

    class Builder {
        private val config: Config = PreviewUseCaseConfig()

        fun setLensFacing(@CameraSelector.LensFacing lensFacing: Int): Builder {
            config.insertOption(OPTION_PREVIEW_LENS_FACING, lensFacing)
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
            CameraXOption.create("ognjenbogicevic.preview.previewView", PreviewView::class.java)

        var OPTION_PREVIEW_LENS_FACING: Option<Int> =
            CameraXOption.createNonMandatory("ognjenbogicevic.preview.lensFacing", Int::class.java)

    }
}

internal class PreviewUseCaseConfig : CameraXUseCaseConfig() {

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
                val defaultConfig = PreviewUseCaseConfig()

                defaultConfig.insertOption(OPTION_PREVIEW_VIEW, null)
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
