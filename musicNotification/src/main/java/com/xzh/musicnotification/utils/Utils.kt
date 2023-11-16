@file:Suppress("DEPRECATION")

package com.xzh.musicnotification.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import java.util.*

object Utils {
    @JvmStatic
    fun getApplicationInfo(context: Context): ApplicationInfo? {
        try {
            return context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun openPermissionSetting(context: Context): Boolean {
        return try {
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //直接跳转到应用通知设置的代码：
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(localIntent)
                return true
            }
            localIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            localIntent.putExtra("app_package", context.packageName)
            localIntent.putExtra("app_uid", context.applicationInfo.uid)
            context.startActivity(localIntent)
            true

            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun openOverlaySetting(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            (context as Activity).startActivityForResult(intent, 0)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Throws(CanceledException::class)
    @JvmStatic
    fun openLock(context: Context, clazz: Class<*>?) {
        try {
            val lockScreen = Intent(context, clazz)
            lockScreen.setPackage(context.packageName)
            lockScreen.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        or Intent.FLAG_FROM_BACKGROUND
                        or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        or Intent.FLAG_ACTIVITY_NO_ANIMATION
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(context, 0, lockScreen, PendingIntent.FLAG_IMMUTABLE)
                    .send()
            } else {
                PendingIntent.getActivity(context, 0, lockScreen, 0).send()
            }
        } catch (e: CanceledException) {
            val lockScreen = Intent(context, clazz)
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lockScreen.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            }
            context.startActivity(lockScreen)
        }
    }

    /**
     * 通过设置全屏，设置状态栏透明
     *
     * @param activity Activity
     */
    @JvmStatic
    fun fullScreen(activity: Activity) {
        val window = activity.window
        //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        val decorView = window.decorView
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = option
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 获取自启动管理页面的Intent
     *
     * @param context context
     * @return 返回自启动管理页面的Intent
     */
    @JvmStatic
    fun getAutostartSettingIntent(context: Context): Intent {
        var componentName: ComponentName? = null
        val brand = Build.MANUFACTURER
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        when (brand.lowercase(Locale.getDefault())) {
            "samsung" -> componentName = ComponentName(
                "com.samsung.android.sm",
                "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"
            )
            "huawei" -> componentName = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
            "xiaomi" -> componentName = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            "vivo" -> //            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                componentName = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            "oppo" -> //            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                componentName = ComponentName(
                    "com.coloros.oppoguardelf",
                    "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
                )
            "yulong", "360" -> componentName = ComponentName(
                "com.yulong.android.coolsafe",
                "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
            )
            "meizu" -> componentName =
                ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
            "oneplus" -> componentName = ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
            "letv" -> {
                intent.action = "com.letv.android.permissionautoboot"
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", context.packageName, null)
            }
            else -> {
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.component = componentName
        return intent
    }

    @JvmStatic
    fun dip2px(dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private var timer: Timer? = null
    @JvmStatic
    fun debounce(doThing: () -> Unit, duration: Long) {
        if (timer != null) timer!!.cancel()
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                doThing.invoke()
                timer = null
            }
        }, duration)
    }

    private fun isPidOfProcessName(context: Context, pid: Int, name: String?): Boolean {
        if (name == null) return false
        var isMain = false
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //遍历所有进程
        for (process in am.runningAppProcesses) {
            if (process.pid == pid) {
                //进程ID相同时判断该进程名是否一致
                if (process.processName.equals(name)) {
                    isMain = true;
                }
                break
            }
        }
        return isMain
    }

    /**
     * 获取主进程名
     * @param context 上下文
     * @return 主进程名
     */
    private fun getMainProcessName(context: Context): String {
        return context.packageManager.getApplicationInfo(context.packageName, 0).processName;
    }

    /**
     * 判断是否主进程
     * @param context 上下文
     * @return true是主进程
     */
    fun isMainProcess(context: Context): Boolean {
        return isPidOfProcessName(context, Process.myPid(), getMainProcessName(context));
    }
}