package com.dipl.cameraxlib

import com.dipl.cameraxlib.config.Option

sealed class CameraXExceptions(private val error: String) : Exception() {
    class PictureAnalyzeException(e: String) : CameraXExceptions(e)
    class ImageCaptureException(e: String) : CameraXExceptions(e)
    class IOException(e: String) : CameraXExceptions(e)
    class DefaultException(e: String) : CameraXExceptions(e)

    override fun toString(): String {
        return super.toString() + "\nMessage: " + error
    }
}

/**
 * [MissingMandatoryConfigParameterException] displays error message about missing mandatory configuration parameter.
 */
class MissingMandatoryConfigParameterException(private val option: Option<*>) : Exception() {
    override fun toString(): String {
        return "Missing parameter: ${option.getOptionId().split(".")[2]}!" +
                "\nMandatory parameter ${option.getOptionId().split(".")[2]} of " +
                "${option.getOptionId().split(".")[1]} use case needs to be set!"
    }
}
