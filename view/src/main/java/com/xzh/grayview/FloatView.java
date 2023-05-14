package com.xzh.grayview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.ui.view.WXFrameLayout;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.weex.extend.DCWXView;

public class FloatView extends DCWXView {
    public MyWXFrameLayout layout;

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

    @UniJSMethod
    public void show() {
        if(layout == null) {
            layout = (MyWXFrameLayout) getHostView();
            ViewGroup parentView = (ViewGroup) layout.getParent();
            parentView.removeView(layout);
        }
        FloatViewUtils.getInstance().show(getContext(), layout);
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
