package com.dipl.cameraxlib

import android.content.Context
import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer

fun Image.toBitmap(width: Int, height: Int): Bitmap? {
    val byteBuffer = this.planes.toByteBuffer(width, height)

    // convert byteBuffer to byteArray
    val yuv420ByteArray = byteBuffer.toByteArray()

    val nv21ByteArray = yuv420ByteArray.toNV21ByteArray(width, height)

    return nv21ByteArray?.nv21ByteArrayToBitmap()
}

/** from firebasevisionimage [com.google.android.gms.internal.firebase_ml.zzsc] */
fun Array<Image.Plane>.toByteBuffer(width: Int, height: Int): ByteBuffer {
    val yBuffer = this[0].buffer
    val uBuffer = this[1].buffer
    val vBuffer = this[2].buffer
    val imageArea = width * height
    val yuvByteArray = ByteArray(3 * (imageArea / 2))
    val vPosition = vBuffer.position()
    val uLimit = uBuffer.limit()
    vBuffer.position(vPosition + 1)
    uBuffer.limit(uLimit - 1)
    val var14 = (vBuffer.remaining() == imageArea / 2 - 2) && (vBuffer.compareTo(uBuffer) == 0)
    vBuffer.position(vPosition)
    uBuffer.limit(uLimit)
    if (var14) {
        yBuffer.get(yuvByteArray, 0, imageArea)
        vBuffer.get(yuvByteArray, imageArea, 1)
        uBuffer.get(yuvByteArray, imageArea + 1, imageArea / 2 - 1)
    } else {
        this[0].fillByteArray(width, height, yuvByteArray, 0, 1)
        this[1].fillByteArray(width, height, yuvByteArray, imageArea + 1, 2)
        this[2].fillByteArray(width, height, yuvByteArray, imageArea, 2)
    }

    return ByteBuffer.wrap(yuvByteArray)
}

private fun Image.Plane.fillByteArray(
    width: Int,
    height: Int,
    yuvByteArray: ByteArray,
    startPosition: Int,
    offset: Int,
) {
    val bufferPosition = buffer.position()
    val var8 = (buffer.remaining() + this.rowStride - 1) / this.rowStride
    val var9 = height / var8
    val var10 = width / var9
    var currentPosition = 0

    var var4 = startPosition

    for (var13 in 0 until var8) {
        var var14 = currentPosition

        for (var15 in 0 until var10) {
            yuvByteArray[var4] = buffer.get(var14)
            var4 += offset
            var14 += pixelStride
        }
        currentPosition += rowStride
    }

    buffer.position(bufferPosition)
}

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}

fun ByteArray.toNV21ByteArray(width: Int, height: Int): ByteArray? {
    val compressToJpegStream = ByteArrayOutputStream()

    val yuvImage = YuvImage(this, ImageFormat.NV21, width, height, null)
    return try {
        // compress NV21 to Jpeg format, because we must create bitmap
        yuvImage.compressToJpeg(
            Rect(0, 0, width, height),
            70,
            compressToJpegStream
        )
        compressToJpegStream.toByteArray()
    } catch (var9: Throwable) {
        null
    } finally {
        compressToJpegStream.close()
    }
}

fun ByteArray.nv21ByteArrayToBitmap(): Bitmap? {
    // decode from ByteArray to Bitmap
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun Bitmap.rotate(degrees: Int): Bitmap {
    // rotate if camera is horizontal
    var bitmap: Bitmap = this
    if (degrees != 0) {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        bitmap.recycle()
        bitmap = rotatedBitmap
    }

    return bitmap
}

internal fun Bitmap.crop_internal(
    decorationAspectRatio: Float,
    decorationWidthRatio: Float,
    decorationHeightRatio: Float,
): Bitmap {

    var croppedHeight = height.toFloat() * decorationHeightRatio
    var croppedWidth = width.toFloat() * decorationWidthRatio

    if (decorationAspectRatio * height < width) {
        croppedWidth = decorationAspectRatio * height * decorationWidthRatio
    } else if (decorationAspectRatio * height > width) {
        croppedHeight = (width / decorationAspectRatio) * decorationHeightRatio
    }
    try {
        return Bitmap.createBitmap(
            this,
            (width.toFloat() / 2f - croppedWidth / 2f).toInt(),
            (height.toFloat() / 2f - croppedHeight / 2f).toInt(),
            croppedWidth.toInt(),
            croppedHeight.toInt()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        throw OBPictureAnalyzeException(
            "Cropping the analyze area failed! " +
                    "Check the validity of forwarded parameters and " +
                    "avoid using crop if you do not work with preview overlay!", e
        )
    }
}

/** Helper function used to create a timestamped file */
fun createFile(baseFolder: File, format: String, extension: String) =
    File(baseFolder, format + extension)

/** Use external media if it is available, our app's file directory otherwise */
fun Context.getDefaultOutputDirectory(): File {
    val appContext = applicationContext
    val mediaDir = externalMediaDirs.firstOrNull()?.let {
        File(it, "default_dir").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}
