package com.dipl.cameraxlib

import com.dipl.cameraxlib.config.Option

sealed class CameraXExceptions(private val error: String, val throwable: Throwable?) : Exception() {
    override fun toString(): String {
        return super.toString() + "\nMessage: " + error
    }
}

class OBPictureAnalyzeException(errorMessage: String, throwable: Throwable? = null) :
    CameraXExceptions(errorMessage, throwable)

class OBImageCaptureException(errorMessage: String, throwable: Throwable? = null) :
    CameraXExceptions(errorMessage, throwable)

class IOException(errorMessage: String, throwable: Throwable? = null) :
    CameraXExceptions(errorMessage, throwable)

class OBDefaultException(errorMessage: String, throwable: Throwable? = null) :
    CameraXExceptions(errorMessage, throwable)

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
