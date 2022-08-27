package com.dipl.cameraxlib.usecase.image_analysis.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dipl.cameraxlib.*
import com.dipl.cameraxlib.usecase.image_analysis.models.ImageCrop

/**
 * Class used to convert image from YUV_420_888 format to bitmap,
 * performs cropping if needed, and calls callback with it.
 */
open class DefaultImageAnalyzer(
    private val imageCrop: ImageCrop?,
    private val analyzeImageBitmap: AnalyzeImageLambda = {},
    analyzeInterval: Long? = DEFAULT_ANALYZE_INTERVAL
) : ImageAnalysis.Analyzer {

    private val analyzeInterval = analyzeInterval ?: DEFAULT_ANALYZE_INTERVAL
    private var lastAnalyzedTimestamp = 0L
    protected var lastAnalyzedFrame: Bitmap? = null

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= analyzeInterval) {

            image.image?.let {

                val bitmap = it.toBitmap(image.width, image.height)

                val rotatedBitmap = bitmap?.rotate(image.imageInfo.rotationDegrees)

                // if bitmap is not null, we call callback with that bitmap
                rotatedBitmap?.let {
                    lastAnalyzedFrame = rotatedBitmap.crop(imageCrop).also(analyzeImageBitmap)
                }
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        image.close()
    }

    companion object {
        private const val DEFAULT_ANALYZE_INTERVAL = 200L
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
                    crop_internal(
                        it.aspectRatio,
                        it.widthRatio,
                        it.heightRatio
                    ).also { recycle() }
                } else {
                    this
                }
            }
        return croppedBitmap
    } catch (e: NullPointerException) {
        e.printStackTrace()
        throw IOException("PreviewView might not be initialized!", e)
    }
}

typealias AnalyzeImageLambda = (image: Bitmap) -> Unit
