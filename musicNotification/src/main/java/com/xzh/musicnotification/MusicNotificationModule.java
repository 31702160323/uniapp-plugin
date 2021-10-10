package com.xzh.musicnotification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.service.PlayServiceV2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;
import io.dcloud.feature.uniapp.utils.UniLogUtils;

import static android.content.Context.BIND_AUTO_CREATE;

public class MusicNotificationModule extends UniModule implements ServiceConnection {
    private WeakReference<PlayServiceV2> mServiceV2;
    private JSONObject mConfig;
    private UniJSCallback mCallback;
    private boolean mLock = false;

    @UniJSMethod(uiThread = false)
    public void init(JSONObject config, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        if (config.get("path") == null) {
            data.put("message", "path不能为空");
            data.put("code", -3);
            callback.invokeAndKeepAlive(data);
            return;
        }

        if (config.get("icon") == null) {
            data.put("message", "icon不能为空");
            data.put("code", -4);
            callback.invokeAndKeepAlive(data);
            return;
        }

        this.mConfig = config;
        this.mCallback = callback;

        Context context = mWXSDKInstance.getContext().getApplicationContext();
        context.bindService(PlayServiceV2.startMusicService(context), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mServiceV2 = new WeakReference<>(((PlayServiceV2.ServiceBinder) iBinder).getInstance());
        mServiceV2.get().initNotification(mConfig);
        mServiceV2.get().setWXSDKInstance(mWXSDKInstance);
        mServiceV2.get().lock(mLock);
        JSONObject data = new JSONObject();
        data.put("message", "设置歌曲信息成功");
        data.put("code", 0);
        UniLogUtils.i("XZH-musicNotification", "初始化成功");
        mCallback.invokeAndKeepAlive(data);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @UniJSMethod(uiThread = false)
    public JSONObject update(JSONObject options) {
        UniLogUtils.i("XZH-musicNotification", "更新UI");
        JSONObject data = new JSONObject();
        boolean isNotification;
        Context context = mWXSDKInstance.getContext().getApplicationContext();
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
        if (mServiceV2 != null) {
            mServiceV2.get().update(options);
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
        if (mServiceV2 != null) mServiceV2.get().playOrPause(options.getBoolean("playing"));
    }

    @UniJSMethod(uiThread = false)
    public void favour(JSONObject options) {
        UniLogUtils.i("XZH-musicNotification", "favour");
        if (mServiceV2 != null) mServiceV2.get().favour(options.getBoolean("favour"));
    }

    @SuppressLint("WrongConstant")
    @UniJSMethod(uiThread = false)
    public void setWidgetStyle(JSONObject options) {
        Intent intent = new Intent("com.xzh.widget.MusicWidget");
        intent.addFlags(0x01000000);
        intent.putExtra("type", "bg");
        intent.putExtra("packageName", mWXSDKInstance.getContext().getPackageName());
        intent.putExtra("bg", options.getString("bg"));
        intent.putExtra("title", options.getString("title"));
        intent.putExtra("tip", options.getString("tip"));
        mWXSDKInstance.getContext().sendBroadcast(intent);
    }

    @UniJSMethod(uiThread = false)
    public boolean openLockActivity(JSONObject options) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Settings.canDrawOverlays(mUniSDKInstance.getContext())) {
            mLock = options.getBoolean("lock");
            if (mServiceV2 != null) mServiceV2.get().lock(mLock);
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
    public void cancel() {
        mWXSDKInstance.getContext().getApplicationContext().unbindService(this);
        PlayServiceV2.stopMusicService(mWXSDKInstance.getContext().getApplicationContext());
    }

    @UniJSMethod(uiThread = false)
    public JSONObject openPermissionSetting() {
        JSONObject data = new JSONObject();
        if (openPermissionSetting(mWXSDKInstance.getContext().getApplicationContext())) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((Activity) mUniSDKInstance.getContext()).startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mUniSDKInstance.getContext().getPackageName())), 0);
        }
    }

    public boolean openPermissionSetting(Context context) {
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

    @UniJSMethod(uiThread = false)
    public void initSongs(UniJSCallback callback) {
        UniLogUtils.i("XZH-musicNotification", "获取歌曲");
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<SongBean> list = new ArrayList<>();
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver resolver = mWXSDKInstance.getContext().getApplicationContext().getContentResolver();
                    Cursor cursor = resolver.query(uri, null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                                SongBean songBean = new SongBean();
                                songBean.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));             //ID
                                songBean.musicName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));        //歌名
                                songBean.musicArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));     //歌手
                                songBean.musicAlbum = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑
                                songBean.musicAlbumID = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));     //专辑ID
                                songBean.musicAlbumURl = "";                                                                       //专辑图片路径
                                songBean.musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));         //路径
                                songBean.musicYear = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));  //发布年份
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    songBean.musicDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //时长
                                }
                                songBean.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));         //文件大小

                                //获取本地音乐专辑图片
                                Cursor cursorTwo = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                                        MediaStore.Audio.Albums._ID + "=?", new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))},
                                        null);

                                if (cursorTwo == null) throw new AssertionError();
                                if (cursorTwo.moveToFirst()) {
                                    String path = cursorTwo.getString(cursorTwo.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                                    if (path != null) songBean.musicAlbumURl = path;
                                }

                                list.add(songBean);

                                cursorTwo.close();
                            }
                        }
                        // 释放资源
                        cursor.close();
                    }
                    callback.invokeAndKeepAlive(list);
                    UniLogUtils.i("XZH-musicNotification", "获取歌曲信息成功");
                }
            }).start();
        } catch (Exception e) {
            callback.invokeAndKeepAlive((new JSONObject()).put("error", e.getMessage()));
            UniLogUtils.e("XZH-musicNotification", "获取歌曲信息失败：" + e.getMessage());
        }
    }

    public static class SongBean {
        public int id;                   //ID
        public String musicName;         //歌名
        public String musicArtist;     //歌手
        public String musicAlbum;        //专辑
        public int musicAlbumID;     //专辑ID
        public String musicAlbumURl = "";//专辑图片路径
        public String musicPath;         //路径
        public String musicYear;         //发布年份
        public String musicDuration;     //时长
        public Long size;                 //文件大小
    }
}
