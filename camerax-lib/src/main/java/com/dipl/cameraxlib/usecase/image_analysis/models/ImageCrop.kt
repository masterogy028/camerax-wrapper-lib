package com.dipl.cameraxlib.usecase.image_analysis.models

import androidx.camera.view.PreviewView
import com.dipl.cameraxlib.OBPictureAnalyzeException
import com.dipl.cameraxlib.ui.RectangleView

/**
 * [aspectRatio] is ratio between height and width of the previewView.
 *
 * [heightRatio] is ratio between height of transparent rect and height of screen.
 *
 * [widthRatio] is ratio between width of transparent rect and width of screen.
 *
 * If you are using [RectangleView] or any other type of overlay
 * make sure that their height and width matches your [PreviewView]'s height and width
 * since you will be cropping a rectangle that lays over your [PreviewView].
 *
 * If you are using crop [WithRatio] make sure that you set the parameters properly.
 */
sealed class ImageCrop {
    abstract val aspectRatio: Float
    abstract val heightRatio: Float
    abstract val widthRatio: Float

    data class WithRectangleView(val view: RectangleView) : ImageCrop() {
        override val heightRatio: Float
            get() = view.heightRatio
        override val widthRatio: Float
            get() = view.widthRatio
        override val aspectRatio: Float
            get() = view.aspectRatio
    }

    data class WithRatio(val hRatio: Float, val wRatio: Float, val aRatio: Float) : ImageCrop() {
        override val widthRatio: Float
            get() = run { if (wRatio == 0f) throw OBPictureAnalyzeException("You can not set the crop width ratio to 0.") else wRatio }
        override val heightRatio: Float
            get() = run { if (hRatio == 0f) throw OBPictureAnalyzeException("You can not set the crop height ratio to 0.") else hRatio }
        override val aspectRatio: Float
            get() = run { if (aRatio == 0f) throw OBPictureAnalyzeException("You can not set the crop aspect ratio to 0.") else aRatio }
    }
}
