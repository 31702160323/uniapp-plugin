package com.xzh.musicnotification.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xzh.musicnotification.LockActivityV3;

import io.dcloud.feature.uniapp.utils.UniLogUtils;

public class Utils {
    public static ApplicationInfo getApplicationInfo(Context context) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static boolean openPermissionSetting(Context context) {
        try {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //直接跳转到应用通知设置的代码：
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                context.startActivity(localIntent);
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                localIntent.putExtra("app_package", context.getPackageName());
                localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
                context.startActivity(localIntent);
                return true;
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                localIntent.addCategory(Intent.CATEGORY_DEFAULT);
                localIntent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(localIntent);
                return true;
            }

            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,

            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(localIntent);
            UniLogUtils.i("XZH-musicNotification", "打开通知权限页成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            UniLogUtils.e("XZH-musicNotification", "打开通知权限页失败：" + e.getMessage());
            return false;
        }
    }

    public static void openOverlaySetting(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            ((Activity) context).startActivityForResult(intent, 0);
        }
    }

    public static void openLock(Context context, Class<?> clazz) {
        try {
            Intent lockScreen = new Intent(context, clazz);
            lockScreen.setPackage(context.getPackageName());
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_FROM_BACKGROUND
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, lockScreen, 0);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Intent lockScreen = new Intent(context, clazz);
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                lockScreen.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            }
            context.startActivity(lockScreen);
        }
    }
    /**
     * 通过设置全屏，设置状态栏透明
     *
     * @param activity Activity
     */
    public static void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);

            } else {
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                attributes.flags |= flagTranslucentStatus;
                window.setAttributes(attributes);
            }
        }
    }
}
