package com.xzh.musicnotification;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.adapter.URIAdapter;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.MusicAsyncQueryHandler;
import com.xzh.musicnotification.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.common.util.BaseInfo;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;
import io.dcloud.feature.uniapp.utils.UniLogUtils;

import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;

public class MusicNotificationModule extends UniModule {
    private JSONObject mConfig;
    private boolean mLock = false;
    private WeakReference<ServiceConnection> connection;
    private WeakReference<PlayServiceV2.ServiceBinder> mBinder;

    @UniJSMethod(uiThread = false)
    public void init(JSONObject config) {
        config.put("icon", mUniSDKInstance.rewriteUri(Uri.parse(config.getString("icon")), URIAdapter.FILE));
        this.mConfig = config;
    }

    @UniJSMethod(uiThread = false)
    public void createNotification(UniJSCallback callback) {
        JSONObject data = new JSONObject();
        data.put("message", "设置歌曲信息成功");
        data.put("code", 0);
        if (this.mConfig.get("path") == null) {
            data.put("message", "path不能为空");
            data.put("code", -3);
            callback.invoke(data);
            return;
        }

        if (this.mConfig.get("icon") == null) {
            data.put("message", "icon不能为空");
            data.put("code", -4);
            callback.invoke(data);
            return;
        }

        connection = new WeakReference<>(new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mBinder = new WeakReference<>((PlayServiceV2.ServiceBinder) iBinder);
                mBinder.get().initNotification(mConfig);
                mBinder.get().setUniSDKInstance(mUniSDKInstance);
                mBinder.get().lock(mLock);
                UniLogUtils.i("XZH-musicNotification", "初始化成功");
                callback.invoke(data);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        });

        Log.d("XZH-musicNotification", "createNotification: " + connection + connection.get());

        Context context = mUniSDKInstance.getContext();
        context.bindService(PlayServiceV2.startMusicService(context), connection.get(), BIND_AUTO_CREATE);
    }

    @UniJSMethod(uiThread = false)
    public JSONObject update(JSONObject options) {
        UniLogUtils.i("XZH-musicNotification", "更新UI");
        JSONObject data = new JSONObject();
        boolean isNotification;
        Context context = mUniSDKInstance.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isNotification = NotificationManagerCompat.from(context).getImportance() != NotificationManager.IMPORTANCE_NONE;
        } else {
            isNotification = NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
        if (!isNotification) {
            data.put("message", "没有通知栏权限");
            data.put("code", -2);
            return data;
        }
        if (mBinder != null) {
            mBinder.get().update(options);
            data.put("message", "设置歌曲信息成功");
            data.put("code", 0);
        } else {
            data.put("message", "请先调用init方法进行初始化操作");
            data.put("code", -1);
        }
        return data;
    }

    @UniJSMethod(uiThread = false)
    public void playOrPause(JSONObject options) {
        if (mBinder != null) mBinder.get().playOrPause(options.getBoolean("playing"));
    }

    @UniJSMethod(uiThread = false)
    public void favour(JSONObject options) {
        UniLogUtils.i("XZH-musicNotification", "favour");
        if (mBinder != null) mBinder.get().favour(options.getBoolean("favour"));
    }

    @UniJSMethod(uiThread = false)
    public void cancel() {
        if (connection != null) {
            mUniSDKInstance.getContext().unbindService(connection.get());
            PlayServiceV2.stopMusicService(mUniSDKInstance.getContext());
            mBinder = null;
            connection = null;
        }
    }

    @UniJSMethod(uiThread = false)
    public void setWidgetStyle(JSONObject options) {
        PlayServiceV2.invoke(mUniSDKInstance.getContext(), "bg", options);
    }

    @UniJSMethod(uiThread = false)
    public boolean openLockActivity(JSONObject options) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Settings.canDrawOverlays(mUniSDKInstance.getContext())) {
            mLock = options.getBoolean("lock");
            if (mBinder != null) mBinder.get().lock(mLock);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", false);
        if (requestCode == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings.canDrawOverlays(mUniSDKInstance.getContext())) {
            map.put("type", true);
        }
        Log.d("XZH-musicNotification", "onActivityResult: " + map.get("type"));
        mUniSDKInstance.fireGlobalEventCallback("openLockActivity", map);
    }

    @UniJSMethod(uiThread = false)
    public JSONObject openPermissionSetting() {
        JSONObject data = new JSONObject();
        if (Utils.openPermissionSetting(mUniSDKInstance.getContext())) {
            data.put("message", "打开应用通知设置页面成功");
            data.put("code", 0);
        } else {
            data.put("message", "打开应用通知设置页面失败");
            data.put("code", -5);
        }
        return data;
    }

    @UniJSMethod(uiThread = false)
    public void openOverlaySetting() {
        Utils.openOverlaySetting(mUniSDKInstance.getContext());
    }

    @UniJSMethod(uiThread = false)
    public void initSongs(UniJSCallback callback) {
        UniLogUtils.i("XZH-musicNotification", "获取歌曲" + System.currentTimeMillis());
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        MusicAsyncQueryHandler asyncQueryHandler = new MusicAsyncQueryHandler(mUniSDKInstance.getContext().getContentResolver(), list -> {
                callback.invoke(list);
                UniLogUtils.i("XZH-musicNotification", "获取歌曲信息成功" + System.currentTimeMillis());
        });
        asyncQueryHandler.startQuery(0, null, uri, null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
    }
}
