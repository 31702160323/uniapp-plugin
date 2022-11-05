package com.xzh.UniPlugin;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.dcloud.application.DCloudApplication;

public class BaseApplication extends DCloudApplication {
    @SuppressLint("SoonBlockedPrivateApi")
    @Override
    public void onCreate() {
        // 如果是系统进程则 hook WebView
        if (Process.myUid() == Process.SYSTEM_UID) {
            int sdkInt = Build.VERSION.SDK_INT;
            try {
                @SuppressLint("PrivateApi") Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
                @SuppressLint("DiscouragedPrivateApi") Field field = factoryClass.getDeclaredField("sProviderInstance");
                field.setAccessible(true);
                Object providerInstance = field.get(null);

                Method getProviderClassMethod = null;
                if (sdkInt > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                } else if (sdkInt == Build.VERSION_CODES.LOLLIPOP_MR1) {
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                }

                if (providerInstance == null && getProviderClassMethod != null) {
                    getProviderClassMethod.setAccessible(true);
                    Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    @SuppressLint("PrivateApi") Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                    Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
                    delegateConstructor.setAccessible(true);
                    assert factoryProviderClass != null;
                    if (sdkInt < Build.VERSION_CODES.O) {//低于Android O版本
                        Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
                        providerConstructor.setAccessible(true);
                        providerInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
                    } else {
                        Field chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD");
                        chromiumMethodName.setAccessible(true);
                        String chromiumMethodNameStr = (String) chromiumMethodName.get(null);
                        if (chromiumMethodNameStr == null) {
                            chromiumMethodNameStr = "create";
                        }
                        Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
                        providerInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
                    }

                    if (providerInstance != null) {
                        field.set("sProviderInstance", providerInstance);
                        Log.e("lmy","Hook success!");
                    } else {
                        Log.e("lmy","Hook failed!");
                    }
                }
            } catch (Exception e) {
                Log.e("lmy", "hookWebView error",e);
            }
        }
        super.onCreate();
    }
}
