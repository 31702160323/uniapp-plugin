package com.xzh.varydesktopicons;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.alibaba.fastjson.JSONObject;
import io.dcloud.PandoraEntry;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

public class VaryDesktopIconsModule extends UniModule {
    String[] componentNameArray = new String[] { "com.xzh.varydesktopicons.wrsicon1", "com.xzh.varydesktopicons.wrsicon2", "com.xzh.varydesktopicons.wrsicon3" };

    ComponentName originComponentName;

    private void disableComponent(ComponentName paramComponentName) {
        PackageManager packageManager = getCurContext().getPackageManager();
        if (packageManager.getComponentEnabledSetting(paramComponentName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            return;
        packageManager.setComponentEnabledSetting(paramComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void enableComponent(ComponentName paramComponentName) {
        PackageManager packageManager = getCurContext().getPackageManager();
        if (packageManager.getComponentEnabledSetting(paramComponentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            return;
        packageManager.setComponentEnabledSetting(paramComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    String getComponentName(String paramString) {
        return "com.xzh.varydesktopicons." + paramString;
    }

    Context getCurContext() {
        return this.mUniSDKInstance.getContext();
    }

    public ComponentName getOriginComponentName() {
        if (this.originComponentName == null)
            this.originComponentName = new ComponentName(getCurContext(), PandoraEntry.class);
        return this.originComponentName;
    }

    @UniJSMethod(uiThread = true)
    public void replaceAppIcon(JSONObject paramJSONObject) {
        String str = paramJSONObject.getString("iconName");
        if (str == null)
            return;
        int i = 0;
        while (true) {
            String[] arrayOfString = this.componentNameArray;
            if (i < arrayOfString.length) {
                String str1 = arrayOfString[i];
                disableComponent(new ComponentName(getCurContext(), str1));
                i++;
                continue;
            }
            disableComponent(getOriginComponentName());
            str = getComponentName(str);
            enableComponent(new ComponentName(getCurContext(), str));
            Boolean bool = paramJSONObject.getBoolean("restartSystemLauncher");
            if (bool != null && bool)
                restartSystemLauncher();
            return;
        }
    }

    @UniJSMethod(uiThread = true)
    public void resetAppIcon() {
        int i = 0;
        while (true) {
            String[] arrayOfString = this.componentNameArray;
            if (i < arrayOfString.length) {
                String str = arrayOfString[i];
                disableComponent(new ComponentName(getCurContext(), str));
                i++;
                continue;
            }
            enableComponent(getOriginComponentName());
            return;
        }
    }

    @UniJSMethod(uiThread = true)
    public void resetAppIconAndRestartSystemLauncher() {
        resetAppIcon();
        restartSystemLauncher();
    }

    public void restartSystemLauncher() {
        Context context = getCurContext();
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        for (ResolveInfo resolveInfo : packageManager.queryIntentActivities(intent, 0)) {
            if (resolveInfo.activityInfo != null)
                activityManager.killBackgroundProcesses(resolveInfo.activityInfo.packageName);
        }
    }
}
