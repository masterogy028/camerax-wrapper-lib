package com.dipl.cameraxlib.usecase

import androidx.camera.core.AspectRatio
import androidx.camera.core.UseCase

/**
 * Abstract class that defines 'OB' prefixed use cases.
 */
abstract class OBUseCase {

    // Field that is used to encapsulate respectful camerax use case
    lateinit var useCase: UseCase

    // standard fields used across all camerax use cases
    protected var screenAspectRatio = AspectRatio.RATIO_16_9
    protected var rotation: Int = 0

    /**
     * Function that generates a camerax use case and assigns it to the [useCase] field.
     *
     * @param pScreenAspectRatio is used to define the generated camerax use case
     * @param pRotation is used to define the generated camerax use case
     */
    abstract fun build(pScreenAspectRatio: Int? = null, pRotation: Int? = null)
}
