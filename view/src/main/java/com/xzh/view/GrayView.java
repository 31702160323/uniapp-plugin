package com.xzh.view;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.ui.view.WXFrameLayout;

import io.dcloud.feature.uniapp.ui.component.UniComponentProp;
import io.dcloud.feature.weex.extend.DCWXView;

public class GrayView extends DCWXView {
    private WXFrameLayout layout;

    public GrayView(WXSDKInstance instance, WXVContainer parent, String instanceId, boolean isLazy, BasicComponentData basicComponentData) {
        super(instance, parent, instanceId, isLazy, basicComponentData);
    }

    public GrayView(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected WXFrameLayout initComponentHostView(@NonNull Context context) {
        this.layout = super.initComponentHostView(context);
        return this.layout;
    }

    @UniComponentProp(name = "sat")
    public void setSat(float sat) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(sat);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        this.layout.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }
}
