package com.xzh.musicnotification;

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
import android.support.v4.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

import java.util.ArrayList;
import java.util.List;

import com.xzh.musicnotification.service.PlayServiceV2;

import static android.content.Context.BIND_AUTO_CREATE;

public class MusicNotificationModule extends UniModule {
    private PlayServiceV2 mServiceV2;
    private ServiceConnection connection;

    @UniJSMethod(uiThread = false)
    public void init(final JSONObject config, final UniJSCallback callback) {
        Context context = mWXSDKInstance.getContext().getApplicationContext();
        JSONObject data = new JSONObject();
        if (config.get("path") == null) {
            data.put("success", "path不能为空");
            data.put("code", -3);
            callback.invoke(data);
            return;
        }

        if (config.get("icon") == null) {
            data.put("success", "icon不能为空");
            data.put("code", -4);
            callback.invoke(data);
            return;
        }


        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mServiceV2 = ((PlayServiceV2.ServiceBinder) iBinder).getInstance();
                mServiceV2.initNotification(config);
                JSONObject data = new JSONObject();
                data.put("success", "设置歌曲信息成功");
                data.put("code", 0);
                callback.invoke(data);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        context.bindService(PlayServiceV2.startMusicService(context).get(), connection, BIND_AUTO_CREATE);
//        try {
//        } catch (Exception e) {
//            Log.d("MusicNotificationModule", e.toString());
//            Log.d("MusicNotificationModule", "-------------------------------------------------");
//            Log.d("MusicNotificationModule", e.getMessage());
//            Log.d("MusicNotificationModule", "-------------------------------------------------");
//            e.printStackTrace();
//        }
    }

    @UniJSMethod(uiThread = false)
    public JSONObject update(JSONObject options) {
        JSONObject data = new JSONObject();
        boolean isNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isNotification = NotificationManagerCompat.from(mWXSDKInstance.getContext().getApplicationContext()).getImportance() != NotificationManager.IMPORTANCE_NONE;
        } else {
            isNotification = NotificationManagerCompat.from(mWXSDKInstance.getContext().getApplicationContext()).areNotificationsEnabled();
        }
        if (!isNotification) {
            data.put("success", "没有通知栏权限");
            data.put("code", -2);
            return data;
        }
        if (mServiceV2 != null) {
            mServiceV2.update(options);
            data.put("success", "设置歌曲信息成功");
            data.put("code", 0);
        } else {
            data.put("success", "未知失败");
            data.put("code", -1);
        }
        return data;
    }

    @UniJSMethod(uiThread = false)
    public void playOrPause(JSONObject options) {
        if (mServiceV2 != null) mServiceV2.playOrPause(options.getBoolean("playing"));
    }

    @UniJSMethod(uiThread = false)
    public void favour(JSONObject options) {
        if (mServiceV2 != null) mServiceV2.favour(options.getBoolean("favour"));
    }

    @UniJSMethod(uiThread = false)
    public void openLockActivity(JSONObject options) {
        if (mServiceV2 != null) mServiceV2.lock(options.getBoolean("lock"));
    }

    @UniJSMethod(uiThread = false)
    public void cancel() {
        if (connection != null) mWXSDKInstance.getContext().getApplicationContext().unbindService(connection);
        PlayServiceV2.stopMusicService(mWXSDKInstance.getContext().getApplicationContext());
    }

    @UniJSMethod(uiThread = false)
    public void playOrPauseCallback(UniJSCallback callback) {
        if (mServiceV2 != null) mServiceV2.addCallback(PlayServiceV2.NotificationReceiver.EXTRA_PLAY, callback);
    }

    @UniJSMethod(uiThread = false)
    public void lastCallback(UniJSCallback callback) {
        if (mServiceV2 != null) mServiceV2.addCallback(PlayServiceV2.NotificationReceiver.EXTRA_PRE,callback);
    }

    @UniJSMethod(uiThread = false)
    public void nextCallback(UniJSCallback callback) {
        if (mServiceV2 != null) mServiceV2.addCallback(PlayServiceV2.NotificationReceiver.EXTRA_NEXT,callback);
    }

    @UniJSMethod(uiThread = false)
    public void favourCallback(UniJSCallback callback) {
        if (mServiceV2 != null) mServiceV2.addCallback(PlayServiceV2.NotificationReceiver.EXTRA_FAV,callback);
    }

    @UniJSMethod(uiThread = false)
    public JSONObject openPermissionSetting() {
        JSONObject data = new JSONObject();
        data.put("success", openPermissionSetting(mWXSDKInstance.getContext().getApplicationContext()));
        data.put("code", 0);
        return data;
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
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @UniJSMethod(uiThread = false)
    public void initSongs(final UniJSCallback callback) {
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
                            songBean.musicDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //时长
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
                callback.invoke(list);
            }
        }).start();
    }

    public static class SongBean {
        public int id;                   //ID
        public String musicName;		 //歌名
        public String musicArtist;  	 //歌手
        public String musicAlbum;        //专辑
        public int musicAlbumID; 	 //专辑ID
        public String musicAlbumURl = "";//专辑图片路径
        public String musicPath;		 //路径
        public String musicYear;         //发布年份
        public String musicDuration;     //时长
        public Long size;			     //文件大小
    }
}
