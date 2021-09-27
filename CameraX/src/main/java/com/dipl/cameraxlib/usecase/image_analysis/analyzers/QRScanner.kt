package com.dipl.cameraxlib.usecase.image_analysis.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.CameraXExceptions
import com.dipl.cameraxlib.crop
import com.dipl.cameraxlib.rotate
import com.dipl.cameraxlib.toBitmap
import com.dipl.cameraxlib.usecase.image_analysis.ImageCrop

class DefaultImageAnalyzer(
    private val imageCrop: ImageCrop?,
    private val analyzeImageBitmap: AnalyzeImageLambda = {},
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy): Bitmap? {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 300) {

            image.image?.let {

                val bitmap = it.toBitmap(image.width, image.height)

                val rotatedBitmap = bitmap?.rotate(image.imageInfo.rotationDegrees)

                // if bitmap is not null, we call callback with that bitmap
                rotatedBitmap?.let {
                    return rotatedBitmap.crop(imageCrop).also(analyzeImageBitmap)
                }
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        image.close()
        return null
    }
}

/**
 * The function crops the bitmap image if necessary and forwards the image to the caller trough [AnalyzeImageLambda] callback.
 *
 * The image will not be cropped if [ImageCrop] parameter of [AnalyzeUseCaseParameters] is null or if there is no preview use case bound to the lifecycle.
 *
 * @throws [CameraXExceptions.PictureAnalyzeException]
 */
fun Bitmap.crop(imageCrop: ImageCrop?): Bitmap {
    try {
        val croppedBitmap: Bitmap =
            imageCrop.let {
                if (it != null) {
                    crop(
                        it.aspectRatio,
                        it.widthRatio,
                        it.heightRatio
                    ).also { recycle() }
                } else {
                    this
                }
            }
        return croppedBitmap
        // parameters[AnalyzeUseCaseParameters.OPTION_ANALYZE_CALLBACK]!!.analyze(croppedBitmap)
    } catch (e: NullPointerException) {
        e.printStackTrace()
        throw CameraXExceptions.IOException("PreviewView might not be initialized!")
    }
}

typealias AnalyzeImageLambda = (image: Bitmap) -> Unit
