package com.dipl.cameraxlib.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View
import com.dipl.cameraxlib.R

/**
 * [RectangleView] is custom CameraX view used for overlay.
 * Used for painting black color around transparent rect,
 * and used for getting params for cropping image.
 * [aspectRatio] is width-to-height ratio of transparent rect.
 * [widthRatio] is ratio between width of transparent rect and width of screen
 * [heightRatio] is ratio between height of transparent rect and height of screen
 */
open class RectangleView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet,
    defaultStyleAttributes: Int = 0,
    defaultStyleResource: Int = 0
) : View(context, attributes, defaultStyleAttributes, defaultStyleResource) {

    private var innerHorizontalPadding: Float = 0f
    private var rectangleHeight: Float = 0f
    private val rectangleWidth: Float by lazy { width.toFloat() - 2 * innerHorizontalPadding }
    private var outerColor: Int = 0
    private lateinit var outerPaint: Paint

    init {
        val styledAttributes = context.obtainStyledAttributes(
            attributes,
            R.styleable.RectangleView,
            defaultStyleAttributes,
            defaultStyleResource
        )

        try {
            innerHorizontalPadding =
                styledAttributes.getDimension(R.styleable.RectangleView_innerHorizontalPadding, 0f)
            rectangleHeight =
                styledAttributes.getDimension(R.styleable.RectangleView_rectangleHeight, 0f)
            outerColor =
                styledAttributes.getColor(R.styleable.RectangleView_outerColor, Color.BLACK)
            outerPaint = Paint()
            outerPaint.color = outerColor
            outerPaint.style = Style.FILL
        } catch (e: Exception) {

        } finally {
            styledAttributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val heightTopRect = height / 2 - rectangleHeight / 2
        val heightBottomRect = height / 2 + rectangleHeight / 2

        // top rect
        canvas.drawRect(0f, 0f, width.toFloat(), heightTopRect, outerPaint)
        // bottom rect
        canvas.drawRect(0f, heightBottomRect, width.toFloat(), height.toFloat(), outerPaint)
        // left rect
        canvas.drawRect(0f, heightTopRect, innerHorizontalPadding, heightBottomRect, outerPaint)
        // right rect
        canvas.drawRect(
            width.toFloat() - innerHorizontalPadding,
            heightTopRect,
            width.toFloat(),
            heightBottomRect,
            outerPaint
        )
    }

    /**
     * [aspectRatio] is width-to-height ratio of transparent rect.
     */
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    /**
     * [widthRatio] is ratio between width of transparent rect and width of screen
     */
    val widthRatio: Float
        get() = rectangleWidth / width.toFloat()

    /**
     * [heightRatio] is ratio between height of transparent rect and height of screen
     */
    val heightRatio: Float
        get() = rectangleHeight / height
}
