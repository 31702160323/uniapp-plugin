package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKInstance;
import com.xzh.musicnotification.LockActivityV2;
import com.xzh.musicnotification.notification.MusicNotificationV2;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.utils.UniLogUtils;
import io.dcloud.feature.uniapp.utils.UniUtils;

import static com.xzh.musicnotification.notification.MusicNotificationV2.NOTIFICATION_ID;

public class PlayServiceV2 extends Service implements MusicNotificationV2.NotificationHelperListener {
    private static PlayServiceV2 serviceV2;
    private static final int FLAGS = 0x01000000;
    private static final String COM_XZH_WIDGET_MUSIC_WIDGET = "com.xzh.widget.MusicWidget";

    private boolean xzhFavour;
    private boolean Favour = false;
    private boolean Playing = false;

    private JSONObject songData;
    private ServiceBinder mBinder;
    private NotificationReceiver mReceiver;
    private WeakReference<LockActivityV2> mActivityV2;
    private WeakReference<WXSDKInstance> mWXSDKInstance;

    public static Intent startMusicService(Context context) {
        Intent intent = new Intent(context, PlayServiceV2.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        return intent;
    }

    public static void stopMusicService(Context context) {
        Intent intent = new Intent(context, PlayServiceV2.class);
        context.stopService(intent);
    }

    @Override
    @SuppressLint("WrongConstant")
    public void onCreate() {
        super.onCreate();
        UniLogUtils.i("XZH-musicNotification","serviceV2 创建成功");
        serviceV2 = this;

        try {
            ApplicationInfo info = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            xzhFavour = info.metaData.getBoolean("xzh_favour");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mReceiver = new NotificationReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(NotificationReceiver.ACTION_STATUS_BAR);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new ServiceBinder();
        return mBinder;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        MusicNotificationV2.getInstance().cancel();
        UniLogUtils.i("XZH-musicNotification","serviceV2 消毁成功");
    }

    @Override
    public void onNotificationInit(Notification notification) {
        // 设置为前台Service
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 接收Notification发送的广播
     */
    public static class NotificationReceiver extends BroadcastReceiver {
        public static final String ACTION_STATUS_BAR = serviceV2.getPackageName() + ".NOTIFICATION_ACTIONS";
        public static final String EXTRA = "extra";
        public static final String EXTRA_PLAY = "play_pause";
        public static final String EXTRA_NEXT = "play_next";
        public static final String EXTRA_PRE = "play_previous";
        public static final String EXTRA_FAV = "play_favourite";
        public boolean lockActivity = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (lockActivity && Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                try {
                    Intent lockScreen = new Intent(context, LockActivityV2.class);
                    lockScreen.setPackage(serviceV2.getPackageName());
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
                    e.printStackTrace();
                    Intent lockScreen = new Intent(context, LockActivityV2.class);
                    lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    lockScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        lockScreen.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    }
                    context.startActivity(lockScreen);
                }
            }
            String extra = intent.getStringExtra(EXTRA);
            if (extra == null) return;
            String eventName = "musicNotificationError";
            Map<String, Object> data = new HashMap<>();
            data.put("message", "触发回调事件成功");
            data.put("code", 0);
            switch (extra) {
                case EXTRA_PLAY:
                    serviceV2.mBinder.playOrPause();
                    UniLogUtils.i("XZH-musicNotification","点击播放按钮");
                    eventName = "musicNotificationPause";
                    break;
                case EXTRA_PRE:
                    UniLogUtils.i("XZH-musicNotification","点击上一首按钮");
                    eventName = "musicNotificationPrevious";
                    break;
                case EXTRA_NEXT:
                    UniLogUtils.i("XZH-musicNotification","点击下一首按钮");
                    eventName = "musicNotificationNext";
                    break;
                case EXTRA_FAV:
                    serviceV2.mBinder.favour();
                    UniLogUtils.i("XZH-musicNotification","点击搜藏按钮");
                    eventName = "musicNotificationFavourite";
                    break;
                default:
                    data.put("message", "触发回调事件失败");
                    data.put("code", -7);
                    break;
            }
            serviceV2.mBinder.fireGlobalEventCallback(eventName, data);
        }
    }

    public class ServiceBinder extends Binder {
        public void setActivity(LockActivityV2 activityV2){
            mActivityV2 = new WeakReference<>(activityV2);
        }

        public void setWXSDKInstance(WXSDKInstance WXSDKInstance){
            mWXSDKInstance = new WeakReference<>(WXSDKInstance);
        }

        public void initNotification(JSONObject config) {
            MusicNotificationV2.getInstance().initNotification(serviceV2, config);
            UniLogUtils.i("XZH-musicNotification","创建通知栏成功");
            favour(Favour);
        }

        public void lock(boolean locking) {
            mReceiver.lockActivity = locking;
        }

        public JSONObject getSongData() {
            return songData;
        }

        public void playOrPause(){
            serviceV2.Playing = !serviceV2.Playing;
            playOrPause(serviceV2.Playing);
        }

        @SuppressLint("WrongConstant")
        public void playOrPause(boolean playing){
            Playing = playing;
            if (mActivityV2 != null && mActivityV2.get() != null) mActivityV2.get().playOrPause(playing);

            Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
            intent.addFlags(FLAGS);
            intent.setPackage(getPackageName());
            intent.putExtra("type", "playOrPause");
            intent.putExtra("playing", playing);
            sendOrderedBroadcast(intent, null);

            MusicNotificationV2.getInstance().playOrPause(playing);
        }

        public void favour(){
            serviceV2.Favour = !serviceV2.Favour;
            favour(serviceV2.Favour);
        }

        @SuppressLint("WrongConstant")
        public void favour(boolean isFavour){
            if (xzhFavour) {
                Favour = isFavour;
                if (mActivityV2 != null && mActivityV2.get() != null) mActivityV2.get().favour(isFavour);

                Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
                intent.addFlags(FLAGS);
                intent.setPackage(getPackageName());
                intent.putExtra("type", "favour");
                intent.putExtra("favour", isFavour);
                sendOrderedBroadcast(intent, null);

                MusicNotificationV2.getInstance().favour(isFavour);
            }
        }

        public boolean getFavour(){
            return serviceV2.Favour;
        }

        public boolean getPlaying(){
            return serviceV2.Playing;
        }

        @SuppressLint("WrongConstant")
        public void update(JSONObject options){
            songData = options;
            Favour = options.getBoolean("favour");
            if (mActivityV2 != null && mActivityV2.get() != null) {
                if (UniUtils.isUiThread()) {
                    mActivityV2.get().updateUI(options);
                } else {
                    mActivityV2.get().runOnUiThread(() -> mActivityV2.get().updateUI(options));
                }
            }

            this.favour(Favour);

            Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
            intent.addFlags(FLAGS);
            intent.setPackage(getPackageName());
            intent.putExtra("type", "update");
            intent.putExtra("songName", options.getString("songName"));
            intent.putExtra("artistsName", options.getString("artistsName"));
            intent.putExtra("picUrl", options.getString("picUrl"));
            sendOrderedBroadcast(intent, null);

            MusicNotificationV2.getInstance().updateSong(options);
        }

        public void fireGlobalEventCallback(String eventName, Map<String, Object> params){
            mWXSDKInstance.get().fireGlobalEventCallback(eventName, params);
        }
    }
}