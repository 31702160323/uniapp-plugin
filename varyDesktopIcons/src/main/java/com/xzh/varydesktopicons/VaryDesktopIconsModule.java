package com.xzh.varydesktopicons;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

public class VaryDesktopIconsModule extends UniModule {
    private final static String PACKAGE_NAME = "com.xzh.varydesktopicons";

    /**
     * 设置默认的别名为启动入口
     */
    @SuppressLint("WrongConstant")
    @UniJSMethod(uiThread = false)
    public void setDefaultAlias() {
        PackageManager packageManager = mUniSDKInstance.getContext().getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext(), "io.dcloud.PandoraEntry"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                0);
        packageManager.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext(), PACKAGE_NAME +
                        ".VaryDesktopIconsActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0);
    }

    /**
     * 设置别名1为启动入口
     */
    @SuppressLint("WrongConstant")
    @UniJSMethod(uiThread = false)
    public void setAlias() {
        PackageManager packageManager = mUniSDKInstance.getContext().getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext(), PACKAGE_NAME +
                        ".VaryDesktopIconsActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                0);
        packageManager.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext(), "io.dcloud.PandoraEntry"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0);
    }
}
