package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

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
    public static final int FLAGS = 0x01000000;
    public final String COM_XZH_WIDGET_MUSIC_WIDGET = "com.xzh.widget.MusicWidget";
    private static PlayServiceV2 serviceV2;
    private JSONObject songData;
    private WeakReference<LockActivityV2> mActivityV2;
    private NotificationReceiver mReceiver;

    public boolean Favour = false;
    public boolean Playing = false;
    public WeakReference<WXSDKInstance> mWXSDKInstance;
    private boolean xzhFavour;

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

    @SuppressLint("WrongConstant")
    @Override
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
        return new ServiceBinder();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceV2 = null;

        unregisterReceiver(mReceiver);
        MusicNotificationV2.getInstance().cancel();
        UniLogUtils.i("XZH-musicNotification","serviceV2 消毁成功");
    }

    @Override
    public void onNotificationInit() {
        startForeground(NOTIFICATION_ID, MusicNotificationV2.getInstance().getNotification());
    }

    public void initNotification(JSONObject config) {
        MusicNotificationV2.getInstance().initNotification(this, config);
        UniLogUtils.i("XZH-musicNotification","创建通知栏成功");
        favour(Favour);
    }

    @SuppressLint("WrongConstant")
    public void update(JSONObject options){
        songData = options;
        Favour = options.getBoolean("favour");
        if (mActivityV2 != null && mActivityV2.get() != null) {
            if (UniUtils.isUiThread()) {
                mActivityV2.get().updateUI(options);
            } else {
                mActivityV2.get().runOnUiThread(() -> {
                    mActivityV2.get().updateUI(options);
                });
            }
        }

        this.favour(Favour);

        Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
        intent.addFlags(FLAGS);
        intent.putExtra("type", "update");
        intent.putExtra("packageName", getPackageName());
        intent.putExtra("songName", options.getString("songName"));
        intent.putExtra("artistsName", options.getString("artistsName"));
        intent.putExtra("picUrl", options.getString("picUrl"));
        sendBroadcast(intent);

        MusicNotificationV2.getInstance().updateSong(options);
    }

    @SuppressLint("WrongConstant")
    public void playOrPause(boolean playing){
        Playing = playing;
        if (mActivityV2 != null && mActivityV2.get() != null) mActivityV2.get().playOrPause(playing);

        Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
        intent.addFlags(FLAGS);
        intent.putExtra("type", "playOrPause");
        intent.putExtra("packageName", getPackageName());
        intent.putExtra("playing", playing);
        sendBroadcast(intent);

        MusicNotificationV2.getInstance().playOrPause(playing);
    }

    @SuppressLint("WrongConstant")
    public void favour(boolean isFavour){
        if (xzhFavour) {
            Favour = isFavour;
            if (mActivityV2 != null && mActivityV2.get() != null) mActivityV2.get().favour(isFavour);

            Intent intent = new Intent(COM_XZH_WIDGET_MUSIC_WIDGET);
            intent.addFlags(FLAGS);
            intent.putExtra("type", "favour");
            intent.putExtra("packageName", getPackageName());
            intent.putExtra("favour", isFavour);
            sendBroadcast(intent);

            MusicNotificationV2.getInstance().favour(isFavour);
        }
    }

    public void lock(boolean locking) {
        mReceiver.lockActivity = locking;
    }

    public void setWXSDKInstance(WXSDKInstance WXSDKInstance){
        this.mWXSDKInstance = new WeakReference<>(WXSDKInstance);
    }

    public JSONObject getSongData() {
        return songData;
    }

    public void setActivity(LockActivityV2 activityV2){
        mActivityV2 = new WeakReference<>(activityV2);
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
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                return;
            }
            if (lockActivity) handleCommandIntent(intent);
            String extra = intent.getStringExtra(EXTRA);
            if (extra == null) return;
            String eventName = "musicNotificationError";
            Map<String, Object> data = new HashMap<>();
            data.put("message", "触发回调事件成功");
            data.put("code", 0);
            switch (extra) {
                case EXTRA_PLAY:
                    serviceV2.Playing = !serviceV2.Playing;
                    serviceV2.playOrPause(serviceV2.Playing);
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
                    serviceV2.Favour = !serviceV2.Favour;
                    serviceV2.favour(serviceV2.Favour);
                    UniLogUtils.i("XZH-musicNotification","点击搜藏按钮");
                    eventName = "musicNotificationFavourite";
                    break;
                default:
                    data.put("message", "触发回调事件失败");
                    data.put("code", -7);
                    break;
            }
            serviceV2.mWXSDKInstance.get().fireGlobalEventCallback(eventName, data);
        }
    }

    private static void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action) ){
            Intent lockScreen = new Intent(serviceV2, LockActivityV2.class);
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            serviceV2.startActivity(lockScreen);
        }
    }

    public class ServiceBinder extends Binder {
        public PlayServiceV2 getInstance(){
            return PlayServiceV2.this;
        }
    }
}