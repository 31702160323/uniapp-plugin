package com.xzh.musicnotification

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.alibaba.fastjson.JSONObject
import com.xzh.musicnotification.service.PlayServiceV2
import com.xzh.musicnotification.utils.MusicAsyncQueryHandler
import com.xzh.musicnotification.utils.MusicAsyncQueryHandler.OnCallbackListener
import com.xzh.musicnotification.utils.Utils.openOverlaySetting
import com.xzh.musicnotification.utils.Utils.openPermissionSetting
import com.xzh.musicnotification.view.FloatView.Companion.instance
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule

class MusicNotificationModule : UniModule() {
    private var isInit = false
    private var lockActivity = false
    private var systemStyle = false
    private var mConfig: JSONObject? = null
    private var connection: ServiceConnection? = null
    private var mBinder: IMusicServiceAidlInterface? = null
    private var createNotificationCallback: UniJSCallback? = null

    //    private UniJSCallback getLocalSongCallback;
    @UniJSMethod(uiThread = false)
    fun init(config: JSONObject) {
        if (config.getString(Global.KEY_PATH) == null) config[Global.KEY_PATH] = ""
        mConfig = config
        isInit = true
    }

    @UniJSMethod(uiThread = false)
    fun createNotification(callback: UniJSCallback?) {
        val activity = mUniSDKInstance.context as Activity
        createNotificationCallback = callback
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.TIRAMISU) {
                //动态申请
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            } else {
                enableNotification(activity)
            }
            return
        }
        cancel()
        val data = JSONObject()
        if (!isInit) {
            data["message"] = "请先调用init方法进行初始化操作"
            data["code"] = -2
            callback!!.invoke(data)
            return
        }
        try {
            connection = object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                    mBinder = IMusicServiceAidlInterface.Stub.asInterface(iBinder)
                    mBinder?.initConfig(mConfig)
                    mBinder?.lock(lockActivity)
                    mBinder?.switchNotification(systemStyle)
                    mBinder?.setServiceEventListener(object :
                        IMusicServiceCallbackAidlInterface.Stub() {
                        override fun sendMessage(eventName: String?, params: MutableMap<Any?, Any?>?
                        ) {
                            val options = JSONObject()
                            params?.forEach { (key, value) ->
                                options[key as String] = value
                            }
                            if (eventName !== null) mUniSDKInstance.fireGlobalEventCallback(eventName, options)
                        }
                    })
                    data["message"] = "设置歌曲信息成功"
                    data["code"] = 0
                    callback!!.invoke(data)
                    createNotificationCallback = null
                    val jsonObject = JSONObject()
                    jsonObject["type"] = "create"
                    mUniSDKInstance.fireGlobalEventCallback(Global.EVENT_MUSIC_LIFECYCLE, jsonObject)
                }

                override fun onServiceDisconnected(componentName: ComponentName) {
                    mBinder = null
                }
            }
            val context = mUniSDKInstance.context

            val intent = Intent(context, PlayServiceV2::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            data["message"] = "创建通知栏失败"
            data["code"] = 0
            callback!!.invoke(data)
        }
    }

    private fun enableNotification(context: Context) {
        try {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            }
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
            (context as Activity).startActivityForResult(intent, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            (context as Activity).startActivityForResult(intent, 1)
        }
    }

    @UniJSMethod(uiThread = false)
    fun update(options: JSONObject): JSONObject {
        val data = JSONObject()
        val isNotification: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(mUniSDKInstance.context).importance != NotificationManager.IMPORTANCE_NONE
        } else {
            NotificationManagerCompat.from(mUniSDKInstance.context)
                .areNotificationsEnabled()
        }
        if (!isNotification) {
            data["message"] = "没有通知栏权限"
            data["code"] = -3
        } else if (!isInit) {
            data["message"] = "请先调用init方法进行初始化操作"
            data["code"] = -2
        } else if (mBinder == null) {
            data["message"] = "请先调用createNotification方法进行初始化操作"
            data["code"] = -1
        } else {
            if (options.getBoolean(Global.KEY_FAVOUR) == null) options[Global.KEY_FAVOUR] = false
            mBinder!!.update(options)
            data["message"] = "设置歌曲信息成功"
            data["code"] = 0
        }
        return data
    }

    @UniJSMethod(uiThread = false)
    fun playOrPause(play: Boolean) {
        if (mBinder != null) {
            mBinder!!.playOrPause(play)
            instance?.playOrPause(play)
        }
    }

    @UniJSMethod(uiThread = false)
    fun favour(isFavour: Boolean) {
        if (mBinder != null) {
            mBinder?.favour = isFavour
            instance?.favour(isFavour)
        }
    }

    @UniJSMethod(uiThread = false)
    fun switchNotification(style: Boolean) {
        systemStyle = style
        if (mBinder != null) mBinder!!.switchNotification(style)
    }

    @UniJSMethod(uiThread = false)
    fun setPosition(position: Int) {
        if (mBinder != null) mBinder!!.setPosition(position.toLong())
    }

    @UniJSMethod(uiThread = false)
    fun cancel() {
        val context = mUniSDKInstance.context
        if (connection != null) {
            hideFloatWindow()
            context.unbindService(connection!!)
            context.stopService(Intent(context, PlayServiceV2::class.java))
            mBinder = null
            connection = null
        }
    }

    @UniJSMethod(uiThread = false)
    fun showFloatWindow(textColor: String?): Boolean {
        if (mBinder != null && checkOverlayDisplayPermission()) {
            instance?.show(mUniSDKInstance, textColor!!)
            return true
        }
        return false
    }

    @UniJSMethod(uiThread = false)
    fun hideFloatWindow(): Boolean {
        if (mBinder != null && checkOverlayDisplayPermission()) {
            instance?.hide()
            return true
        }
        return false
    }

    @UniJSMethod(uiThread = false)
    fun setLyric(lyric: String?) {
        instance?.setLyric(lyric)
    }

    @UniJSMethod(uiThread = false)
    fun setWidgetStyle(options: JSONObject) {
        try {
            val clazz = Class.forName("com.xzh.widget.MusicWidget")

            val method = clazz.getDeclaredMethod(
                "invoke",
                Context::class.java,
                String::class.java,
                MutableMap::class.java
            )
            method.invoke(clazz, mUniSDKInstance.context, "bg", options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @UniJSMethod(uiThread = false)
    fun openLockActivity(`is`: Boolean): Boolean {
        if (checkOverlayDisplayPermission()) {
            lockActivity = `is`
            if (mBinder != null) mBinder!!.lock(lockActivity)
            return true
        }
        return false
    }

    @UniJSMethod(uiThread = false)
    fun checkOverlayDisplayPermission(): Boolean {
        // API23以后需要检查权限
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(mUniSDKInstance.context)
        } else {
            true
        }
    }

    @UniJSMethod(uiThread = false)
    fun openPermissionSetting(): JSONObject {
        val data = JSONObject()
        if (openPermissionSetting(mUniSDKInstance.context)) {
            data["message"] = "打开应用通知设置页面成功"
            data["code"] = 0
        } else {
            data["message"] = "打开应用通知设置页面失败"
            data["code"] = -5
        }
        return data
    }

    @UniJSMethod(uiThread = false)
    fun openOverlaySetting() {
        openOverlaySetting(mUniSDKInstance.context)
    }

    @UniJSMethod(uiThread = false)
    fun getLocalSong(callback: UniJSCallback) {
//        this.getLocalSongCallback = callback;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT >= mUniSDKInstance.getContext().getApplicationInfo().targetSdkVersion) {
//            if (ActivityCompat.checkSelfPermission((Activity) mUniSDKInstance.getContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions((Activity) mUniSDKInstance.getContext(),new String[]{Manifest.permission.READ_MEDIA_AUDIO},2);
//                return;
//            }
//        } else if(ActivityCompat.checkSelfPermission((Activity) mUniSDKInstance.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) mUniSDKInstance.getContext(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
//            return;
//        }
        MusicAsyncQueryHandler(mUniSDKInstance.context.contentResolver)
            .setOnCallbackListener(object : OnCallbackListener {
                override fun onCallbackListener(list: ArrayList<Any>) {
                    callback.invoke(list)
                }
            })
            .startQuery()
        //        this.getLocalSongCallback = null;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                val map = JSONObject()
                map["type"] =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(
                        mUniSDKInstance.context
                    )
                mUniSDKInstance.fireGlobalEventCallback(Global.EVENT_OPEN_LOCK_ACTIVITY, map)
            }
            1 -> if (NotificationManagerCompat.from(mUniSDKInstance.context)
                    .areNotificationsEnabled()
            ) {
                createNotification(createNotificationCallback)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (NotificationManagerCompat.from(mUniSDKInstance.context)
                    .areNotificationsEnabled()
            ) {
                createNotification(createNotificationCallback)
            }
        }
    }

    override fun onActivityDestroy() {
        super.onActivityDestroy()
        cancel()
    }
}