package com.xzh.musicnotification;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.notification.MusicNotificationV2;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.MusicAsyncQueryHandler;
import com.xzh.musicnotification.utils.Utils;
import com.xzh.musicnotification.view.FloatView;

import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class MusicNotificationModule extends UniModule implements PlayServiceV2.OnEventListener {
    private boolean isInit;
    private boolean showFavour;
    private boolean lockActivity;
    private boolean systemStyle;
    private ServiceConnection connection;
    private PlayServiceV2.ServiceBinder mBinder;
    private UniJSCallback createNotificationCallback;
//    private UniJSCallback getLocalSongCallback;

    @UniJSMethod(uiThread = false)
    public void init(JSONObject config) {
        if (config.getString(Global.KEY_PATH) == null) config.put(Global.KEY_PATH, "");

        ApplicationInfo info = Utils.getApplicationInfo(mUniSDKInstance.getContext());
        if (info != null) {
            showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR);
        }

        MusicNotificationV2.init(config);
        MusicNotificationV2.setShowFavour(showFavour);
        isInit = true;
    }

    @UniJSMethod(uiThread = false)
    public void createNotification(UniJSCallback callback) {
        Activity activity = (Activity) mUniSDKInstance.getContext();
        this.createNotificationCallback = callback;
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.TIRAMISU) {
                //动态申请
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            } else {
                enableNotification(activity);
            }
            return;
        }
        this.cancel();
        JSONObject data = new JSONObject();
        if (!isInit) {
            data.put("message", "请先调用init方法进行初始化操作");
            data.put("code", -2);
            callback.invoke(data);
            return;
        }
        try {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mBinder = (PlayServiceV2.ServiceBinder) iBinder;
                    mBinder.lock(lockActivity);
                    mBinder.switchNotification(systemStyle);
                    mBinder.setEventListener(MusicNotificationModule.this);
                    data.put("message", "设置歌曲信息成功");
                    data.put("code", 0);
                    callback.invoke(data);
                    createNotificationCallback = null;

                    JSONObject object = new JSONObject();
                    object.put("type", "create");
                    mUniSDKInstance.fireGlobalEventCallback(Global.EVENT_MUSIC_LIFECYCLE, object);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };

            Context context = mUniSDKInstance.getContext();
            context.bindService(PlayServiceV2.startMusicService(context), connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            data.put("message", "创建通知栏失败");
            data.put("code", 0);
            callback.invoke(data);
        }
    }

    public static void enableNotification(Context context) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            }
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            ((Activity) context).startActivityForResult(intent, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            ((Activity) context).startActivityForResult(intent, 1);
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
        } else if (!isInit) {
            data.put("message", "请先调用init方法进行初始化操作");
            data.put("code", -2);
        } else if (mBinder == null) {
            data.put("message", "请先调用createNotification方法进行初始化操作");
            data.put("code", -1);
        } else {
            if (options.getBoolean(Global.KEY_FAVOUR) == null)
                options.put(Global.KEY_FAVOUR, false);
            mBinder.update(options);
            data.put("message", "设置歌曲信息成功");
            data.put("code", 0);
        }
        return data;
    }

    @UniJSMethod(uiThread = false)
    public void playOrPause(boolean is) {
        if (mBinder != null) {
            mBinder.playOrPause(is);
            FloatView.getInstance().playOrPause(is);
        }
    }

    @UniJSMethod(uiThread = false)
    public void favour(boolean is) {
        if (!showFavour) return;
        if (mBinder != null) {
            mBinder.favour(is);
            FloatView.getInstance().favour(is);
        }
    }

    @UniJSMethod(uiThread = false)
    public void switchNotification(boolean is) {
        systemStyle = is;
        if (mBinder != null) mBinder.switchNotification(systemStyle);
    }

    @UniJSMethod(uiThread = false)
    public void cancel() {
        if (connection != null) {
            hideFloatWindow();
            mUniSDKInstance.getContext().unbindService(connection);
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
    public boolean openLockActivity(boolean is) {
        if (checkOverlayDisplayPermission()) {
            lockActivity = is;
            if (mBinder != null) mBinder.lock(lockActivity);
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
    public void getLocalSong(UniJSCallback callback) {
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
        new MusicAsyncQueryHandler(mUniSDKInstance.getContext().getContentResolver())
                .setOnCallbackListener(callback::invoke)
                .startQuery();
//        this.getLocalSongCallback = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: " + requestCode);
        switch (requestCode) {
            case 0:
                JSONObject map = new JSONObject();
                map.put("type", Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(mUniSDKInstance.getContext()));
                mUniSDKInstance.fireGlobalEventCallback(Global.EVENT_OPEN_LOCK_ACTIVITY, map);
                break;
            case 1:
                if (NotificationManagerCompat.from(mUniSDKInstance.getContext()).areNotificationsEnabled()) {
                    this.createNotification(this.createNotificationCallback);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (NotificationManagerCompat.from(mUniSDKInstance.getContext()).areNotificationsEnabled()) {
                    this.createNotification(this.createNotificationCallback);
                }
                break;
//            case 2:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT >= mUniSDKInstance.getContext().getApplicationInfo().targetSdkVersion) {
//                    if (ActivityCompat.checkSelfPermission((Activity) mUniSDKInstance.getContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//                        this.getLocalSong(this.getLocalSongCallback);
//                    }
//                } else if(ActivityCompat.checkSelfPermission((Activity) mUniSDKInstance.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    this.getLocalSong(this.getLocalSongCallback);
//                }
//                break;
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        this.cancel();
    }

    @Override
    public void sendMessage(String eventName, Map<String, Object> params) {
        mUniSDKInstance.fireGlobalEventCallback(eventName, params);
    }
}
