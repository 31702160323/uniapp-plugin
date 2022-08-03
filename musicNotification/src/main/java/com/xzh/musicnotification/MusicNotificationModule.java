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

import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.MusicAsyncQueryHandler;
import com.xzh.musicnotification.utils.Utils;
import com.xzh.musicnotification.view.FloatView;

import java.lang.ref.WeakReference;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

import static android.content.Context.BIND_AUTO_CREATE;

public class MusicNotificationModule extends UniModule {
    private JSONObject mConfig;
    private boolean mLock;
    private boolean mSystemStyle;
    private WeakReference<ServiceConnection> connection;
    private WeakReference<PlayServiceV2.ServiceBinder> mBinder;

    @UniJSMethod(uiThread = false)
    public void init(JSONObject config) {
        if (config.getString(Global.KEY_PATH) == null) config.put(Global.KEY_PATH, "");
        this.mConfig = config;
    }

    @UniJSMethod(uiThread = false)
    public void createNotification(UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            connection = new WeakReference<>(new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mBinder = new WeakReference<>((PlayServiceV2.ServiceBinder) iBinder);
                    mBinder.get().initNotification(mConfig);
                    mBinder.get().setUniSDKInstance(mUniSDKInstance);
                    mBinder.get().lock(mLock);
                    mBinder.get().switchNotification(mSystemStyle);
                    data.put("message", "设置歌曲信息成功");
                    data.put("code", 0);
                    callback.invoke(data);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            });

            Context context = mUniSDKInstance.getContext();
            context.bindService(PlayServiceV2.startMusicService(context), connection.get(), BIND_AUTO_CREATE);
        } catch (Exception e) {
            data.put("message", "创建通知栏失败");
            data.put("code", 0);
            callback.invoke(data);
        }
    }

    @UniJSMethod(uiThread = false)
    public JSONObject update(JSONObject options) {
        JSONObject data = new JSONObject();
        boolean isNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isNotification = NotificationManagerCompat.from(mUniSDKInstance.getContext()).getImportance() != NotificationManager.IMPORTANCE_NONE;
        } else {
            isNotification = NotificationManagerCompat.from(mUniSDKInstance.getContext()).areNotificationsEnabled();
        }
        if (!isNotification) {
            data.put("message", "没有通知栏权限");
            data.put("code", -3);
        } else if(mConfig == null) {
            data.put("message", "请先调用init方法进行初始化操作");
            data.put("code", -2);
        } else if (mBinder == null) {
            data.put("message", "请先调用createNotification方法进行初始化操作");
            data.put("code", -1);
        } else {
            if (options.getBoolean(Global.KEY_FAVOUR) == null)  options.put(Global.KEY_FAVOUR, false);
            mBinder.get().update(options);
            data.put("message", "设置歌曲信息成功");
            data.put("code", 0);
        }
        return data;
    }

    @UniJSMethod(uiThread = false)
    public void playOrPause(JSONObject options) {
        if (mBinder != null) {
            mBinder.get().playOrPause(options.getBoolean(Global.KEY_PLAYING));
            FloatView.getInstance().playOrPause(options.getBoolean(Global.KEY_PLAYING));
        }
    }

    @UniJSMethod(uiThread = false)
    public void favour(JSONObject options) {
        if (mBinder != null) {
            mBinder.get().favour(options.getBoolean(Global.KEY_FAVOUR));
            FloatView.getInstance().favour(options.getBoolean(Global.KEY_FAVOUR));
        }
    }

    @UniJSMethod(uiThread = false)
    public void switchNotification(boolean is) {
        mSystemStyle = is;
        if (mBinder != null) mBinder.get().switchNotification(mSystemStyle);
    }

    @UniJSMethod(uiThread = false)
    public void cancel() {
        if (connection != null) {
            hideFloatWindow();
            mUniSDKInstance.getContext().unbindService(connection.get());
            PlayServiceV2.stopMusicService(mUniSDKInstance.getContext());
            mBinder = null;
            connection = null;
        }
    }

    @UniJSMethod(uiThread = false)
    public boolean showFloatWindow(String textColor) {
        if (mBinder != null && checkOverlayDisplayPermission()) {
            FloatView.getInstance().show(mUniSDKInstance, textColor);
            return true;
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public boolean hideFloatWindow() {
        if (mBinder != null && checkOverlayDisplayPermission()) {
            FloatView.getInstance().hide();
            return true;
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public void setLyric(String lyric) {
        FloatView.getInstance().setLyric(lyric);
    }

    @UniJSMethod(uiThread = false)
    public void setWidgetStyle(JSONObject options) {
        PlayServiceV2.invoke(mUniSDKInstance.getContext(), "bg", options);
    }

    @UniJSMethod(uiThread = false)
    public boolean openLockActivity(JSONObject options) {
        if(checkOverlayDisplayPermission()) {
            mLock = options.getBoolean(Global.KEY_LOCK);
            if (mBinder != null) mBinder.get().lock(mLock);
            return true;
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public boolean checkOverlayDisplayPermission() {
        // API23以后需要检查权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mUniSDKInstance.getContext());
        } else {
            return true;
        }
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
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        MusicAsyncQueryHandler asyncQueryHandler = new MusicAsyncQueryHandler(mUniSDKInstance.getContext().getContentResolver(), callback::invoke);
        asyncQueryHandler.startQuery(0, null, uri, null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        JSONObject map = new JSONObject();
        map.put("type", requestCode == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings.canDrawOverlays(mUniSDKInstance.getContext()));
        mUniSDKInstance.fireGlobalEventCallback(Global.EVENT_OPEN_LOCK_ACTIVITY, map);
    }
}
