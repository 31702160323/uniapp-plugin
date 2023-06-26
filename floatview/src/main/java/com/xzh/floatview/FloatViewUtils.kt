package com.xzh.floatview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.ArrayMap
import android.view.*
import android.view.View.OnTouchListener

class FloatViewUtils private constructor() {
    private val windowManagers = ArrayMap<ViewGroup?, WindowManager?>()
    private val floatLps = ArrayMap<ViewGroup, WindowManager.LayoutParams>()
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show(context: Context, view: ViewGroup?, absoluteX: Int, absoluteY: Int) {
        if (windowManagers[view] == null && view != null) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            var floatLp = floatLps[view]
            if (floatLp == null) {
                floatLp = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,  // 全局
                    // Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST,
                    // 应用内
                    WindowManager.LayoutParams.FIRST_SUB_WINDOW,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                floatLp.let {
                    it.gravity = Gravity.LEFT or Gravity.TOP
                    it.x = absoluteX
                    it.y = absoluteY
                }
            }
            val touchListener: OnTouchListener = object : OnTouchListener {
                private var lastX = 0.0
                private var lastY = 0.0
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (lastX == 0.0 || lastY == 0.0) {
                                lastX = event.rawX.toDouble()
                                lastY = event.rawY.toDouble()
                            }
                            val nowX = event.rawX.toDouble()
                            val nowY = event.rawY.toDouble()
                            val movedX = nowX - lastX
                            val movedY = nowY - lastY
                            floatLp.x += movedX.toInt()
                            floatLp.y += movedY.toInt()
                            windowManager.updateViewLayout(view, floatLp)
                            lastX = nowX
                            lastY = nowY
                        }
                        MotionEvent.ACTION_UP -> {
                            lastX = 0.0
                            lastY = 0.0
                        }
                    }
                    return true
                }
            }
            view.setOnTouchListener(touchListener)
            windowManager.addView(view, floatLp)
            floatLps[view] = floatLp
            windowManagers[view] = windowManager
        }
    }

    fun hide(view: ViewGroup?) {
        val windowManager = windowManagers[view]
        if (view != null) {
            windowManager?.removeView(view)
            windowManagers.remove(view)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: FloatViewUtils? = null
        @JvmStatic
        val instance: FloatViewUtils?
            get() {
                if (singleton == null) {
                    synchronized(FloatViewUtils::class.java) {
                        if (singleton == null) {
                            singleton = FloatViewUtils()
                        }
                    }
                }
                return singleton
            }
    }
}