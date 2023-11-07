package com.xzh.musicnotification.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Scroller
import kotlin.math.abs

/**
 * @author ztk
 */
class SlidingFinishLayout : RelativeLayout {
    /**
     * 滑动的最小距离
     */
    private var mTouchSlop = 0
    private var mScroller: Scroller? = null

    /**
     * 父布局
     */
    private var mParentView: ViewGroup? = null

    /**
     * 按下X坐标
     */
    private var downX = 0

    /**
     * 按下Y坐标
     */
    private var downY = 0

    /**
     * 临时存X坐标
     */
    private var tempX = 0
    private var viewWidth = 0

    /**
     * 是否正在滑动
     */
    private var isSliding = false
    private var onSlidingFinishListener: OnSlidingFinishListener? = null
    private var isFinish = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mScroller = Scroller(context)
    }

    interface OnSlidingFinishListener {
        /**
         * 滑动销毁页面回调
         */
        fun onSlidingFinish()
    }

    fun setOnSlidingFinishListener(onSlidingFinishListener: OnSlidingFinishListener?) {
        this.onSlidingFinishListener = onSlidingFinishListener
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            // 获取SlidingFinishLayout布局的父布局
            mParentView = this.parent as ViewGroup
            viewWidth = this.width
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                run {
                    tempX = event.rawX.toInt()
                    downX = tempX
                }
                downY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.rawX.toInt()
                val deltaX = tempX - moveX
                tempX = moveX
                if (abs(moveX - downX) > mTouchSlop
                    && abs(event.rawY.toInt() - downY) < mTouchSlop
                ) {
                    isSliding = true
                }
                if (moveX - downX >= 0 && isSliding) {
                    mParentView!!.scrollBy(deltaX, 0)
                }
            }
            MotionEvent.ACTION_UP -> {
                isSliding = false
                if (mParentView!!.scrollX <= -viewWidth / 4) {
                    isFinish = true
                    scrollRight()
                } else {
                    scrollOrigin()
                    isFinish = false
                }
            }
            else -> {}
        }
        return true
    }

    private fun scrollRight() {
        val delta = viewWidth + mParentView!!.scrollX
        //滚动出界面
        mScroller!!.startScroll(
            mParentView!!.scrollX, 0, -delta + 1, 0,
            abs(delta)
        )
        postInvalidate()
    }

    private fun scrollOrigin() {
        val delta = mParentView!!.scrollX
        //滚动到起始位置
        mScroller!!.startScroll(
            mParentView!!.scrollX, 0, -delta, 0,
            abs(delta)
        )
        postInvalidate()
    }

    override fun computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (mScroller!!.computeScrollOffset()) {
            mParentView!!.scrollTo(mScroller!!.currX, mScroller!!.currY)
            postInvalidate()
            if (mScroller!!.isFinished) {
                if (onSlidingFinishListener != null && isFinish) {
                    onSlidingFinishListener!!.onSlidingFinish()
                }
            }
        }
    }
}