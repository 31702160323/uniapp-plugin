package com.xzh.floatview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.ui.view.WXFrameLayout;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.utils.UniViewUtils;
import io.dcloud.feature.weex.extend.DCWXView;

public class FloatView extends DCWXView {
    public MyWXFrameLayout layout;
    private int headHeight;

    public FloatView(WXSDKInstance instance, WXVContainer parent, String instanceId, boolean isLazy, BasicComponentData basicComponentData) {
        super(instance, parent, instanceId, isLazy, basicComponentData);
    }

    public FloatView(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected MyWXFrameLayout initComponentHostView(@NonNull Context context) {
        MyWXFrameLayout frameLayout;
        if (layout == null) {
            frameLayout = new MyWXFrameLayout(context);
        } else {
            frameLayout = layout;
        }
        frameLayout.holdComponent(this);
        frameLayout.removeAllViews();
        return frameLayout;
    }

    @Override
    protected void onHostViewInitialized(WXFrameLayout host) {
        super.onHostViewInitialized(host);
        getDisplayCutoutInfo((Activity) getContext());
        headHeight += UniViewUtils.getScreenHeight(getContext()) - UniViewUtils.getUniHeight(getInstanceId());
    }

    @UniJSMethod
    public void show() {
        if(layout == null) {
            layout = (MyWXFrameLayout) getHostView();
            ViewGroup parentView = (ViewGroup) layout.getParent();
            parentView.removeView(layout);
        }
        FloatViewUtils.getInstance().show(getContext(), layout, getAbsoluteX(), getAbsoluteY() + headHeight);
    }

    @UniJSMethod
    public void hide() {
        FloatViewUtils.getInstance().hide(layout);
    }

    @Override
    public void destroy() {
        super.destroy();
        hide();
        layout = null;
    }

    private void getDisplayCutoutInfo(Activity activity) {
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }

        final View decorView = activity.getWindow().getDecorView();
        // 主动触发onApplyWindowInsets回调
        decorView.requestApplyInsets();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            // 注意直接从insets中获取getDisplayCutout（）会出现为null现象，导致获取不到挖孔信息
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                WindowInsets rootWindowInsets = v.getRootWindowInsets();
                if (rootWindowInsets != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
                        if (displayCutout != null) {
                            // 可根据屏幕旋转的情况获取对应的值
                            headHeight += displayCutout.getSafeInsetTop();
                        }
                    }
                }
                // 设置为null可以防止多次回调；如果不设置，应该要保证onApplyWindowInsets回调里面的逻辑具有幂等性
                decorView.setOnApplyWindowInsetsListener(null);
            }
            return insets;
        });
    }

    static class MyWXFrameLayout extends WXFrameLayout {
        private double px = 0.0f;
        private double py = 0.0f;

        public MyWXFrameLayout(Context context) {
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    px = event.getRawX();
                    py = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getRawX() - px > 10 || event.getRawY() - py > 10) {
                        return true;
                    }
                    break;
            }
            return super.onInterceptTouchEvent(event);
        }
    }
}
