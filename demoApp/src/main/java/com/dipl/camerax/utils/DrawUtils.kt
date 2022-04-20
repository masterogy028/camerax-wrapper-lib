package com.dipl.camerax.utils

import android.graphics.*
import androidx.camera.core.CameraSelector
import com.google.mlkit.vision.face.Face

fun drawRect(
    drawBitmap: Bitmap,
    originalBitmap: Bitmap,
    face: Face,
    lensFacing: Int
): Bitmap {
    val resultBitmap = Bitmap.createBitmap(
        drawBitmap.width,
        drawBitmap.height,
        drawBitmap.config
    )

    val pen = Paint()
    pen.color = Color.RED
    pen.style = Paint.Style.STROKE
    pen.strokeWidth = 8.0f
    val canvas = Canvas(resultBitmap)
    canvas.drawBitmap(drawBitmap, 0.0f, 0.0f, null)

    val rect = when (lensFacing) {
        CameraSelector.LENS_FACING_FRONT -> Rect(
            drawBitmap.width - (face.boundingBox.right * (drawBitmap.width.toFloat() / originalBitmap.width.toFloat())).toInt(),
            (face.boundingBox.top * (drawBitmap.height.toFloat() / originalBitmap.height.toFloat())).toInt(),
            drawBitmap.width - (face.boundingBox.left * (drawBitmap.width.toFloat() / originalBitmap.width.toFloat())).toInt(),
            (face.boundingBox.bottom * (drawBitmap.height.toFloat() / originalBitmap.height.toFloat())).toInt()
        )
        else -> Rect(
            (face.boundingBox.right * (drawBitmap.width.toFloat() / originalBitmap.width.toFloat())).toInt(),
            (face.boundingBox.top * (drawBitmap.height.toFloat() / originalBitmap.height.toFloat())).toInt(),
            (face.boundingBox.left * (drawBitmap.width.toFloat() / originalBitmap.width.toFloat())).toInt(),
            (face.boundingBox.bottom * (drawBitmap.height.toFloat() / originalBitmap.height.toFloat())).toInt()
        )
    }

    //canvas.drawRect(face.boundingBox, pen)
    canvas.drawRect(rect, pen)
    return resultBitmap
}
