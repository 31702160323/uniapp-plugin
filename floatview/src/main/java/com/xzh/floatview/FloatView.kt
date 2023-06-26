package com.xzh.floatview

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.ui.action.BasicComponentData
import com.taobao.weex.ui.component.WXVContainer
import com.taobao.weex.ui.view.WXFrameLayout
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.utils.UniViewUtils
import io.dcloud.feature.weex.extend.DCWXView

class FloatView : DCWXView {
    private var layout: MyWXFrameLayout? = null
    private var headHeight = 0

    constructor(
        instance: WXSDKInstance?,
        parent: WXVContainer<*>?,
        instanceId: String?,
        isLazy: Boolean,
        basicComponentData: BasicComponentData<*>?
    ) : super(instance, parent, instanceId, isLazy, basicComponentData)

    constructor(
        instance: WXSDKInstance?,
        parent: WXVContainer<*>?,
        basicComponentData: BasicComponentData<*>?
    ) : super(instance, parent, basicComponentData)

    override fun initComponentHostView(context: Context): MyWXFrameLayout {
        val frameLayout: MyWXFrameLayout = if (layout == null) {
            MyWXFrameLayout(context)
        } else {
            layout!!
        }
        frameLayout.holdComponent(this)
        frameLayout.removeAllViews()
        return frameLayout
    }

    override fun onHostViewInitialized(host: WXFrameLayout) {
        super.onHostViewInitialized(host)
        getDisplayCutoutInfo(context as Activity)
        headHeight += UniViewUtils.getScreenHeight(context) - UniViewUtils.getUniHeight(instanceId)
    }

    override fun updateNativeStyle(key: String, value: Any) {
        super.updateNativeStyle(key, value)
    }

    @UniJSMethod
    fun show() {
        if (layout == null) {
            layout = hostView as MyWXFrameLayout
            val parentView = layout!!.parent as ViewGroup
            parentView.removeView(layout)
        }
        FloatViewUtils.instance!!.show(context, layout, absoluteX, absoluteY + headHeight)
    }

    @UniJSMethod
    fun hide() {
        FloatViewUtils.instance!!.hide(layout)
    }

    override fun destroy() {
        FloatViewUtils.instance!!.hide(layout)
        super.destroy()
        layout = null
    }

    private fun getDisplayCutoutInfo(activity: Activity?) {
        if (activity == null || activity.isDestroyed || activity.isFinishing) {
            return
        }
        val decorView = activity.window.decorView
        // 主动触发onApplyWindowInsets回调
        decorView.requestApplyInsets()
        decorView.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets? ->
            // 注意直接从insets中获取getDisplayCutout（）会出现为null现象，导致获取不到挖孔信息
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val rootWindowInsets = v.rootWindowInsets
                if (rootWindowInsets != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val displayCutout = rootWindowInsets.displayCutout
                        if (displayCutout != null) {
                            // 可根据屏幕旋转的情况获取对应的值
                            headHeight += displayCutout.safeInsetTop
                        }
                    }
                }
                // 设置为null可以防止多次回调；如果不设置，应该要保证onApplyWindowInsets回调里面的逻辑具有幂等性
                decorView.setOnApplyWindowInsetsListener(null)
            }
            insets!!
        }
    }

    class MyWXFrameLayout(context: Context?) : WXFrameLayout(context) {
        private var px = 0.0
        private var py = 0.0
        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    px = event.rawX.toDouble()
                    py = event.rawY.toDouble()
                }
                MotionEvent.ACTION_MOVE -> if (event.rawX - px > 10 || event.rawY - py > 10) {
                    return true
                }
            }
            return super.onInterceptTouchEvent(event)
        }
    }
}