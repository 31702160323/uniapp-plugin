package com.xzh.musicnotification.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * @author ztk
 */
class HintTextView : AppCompatTextView {
    private var paint: Paint? = null
    private var mWidth = 0
    private var gradient: LinearGradient? = null
    private var matrix: Matrix? = null

    /**
     * 渐变的速度
     */
    private var deltaX = 0

    constructor(context: Context?) : super(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        paint = getPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mWidth == 0) {
            mWidth = measuredWidth
            //颜色渐变器
            gradient = LinearGradient(
                0f,
                0f,
                mWidth.toFloat(),
                0f,
                intArrayOf(Color.GRAY, Color.WHITE, Color.GRAY),
                floatArrayOf(
                    0.3f, 0.5f, 1.0f
                ),
                Shader.TileMode.CLAMP
            )
            paint!!.shader = gradient
            matrix = Matrix()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (matrix != null) {
            deltaX += mWidth / 8
            if (deltaX > 2 * mWidth) {
                deltaX = -mWidth
            }

            //通过矩阵的平移实现
            matrix!!.setTranslate(deltaX.toFloat(), 0f)
            gradient!!.setLocalMatrix(matrix)
            postInvalidateDelayed(100)
        }
    }
}