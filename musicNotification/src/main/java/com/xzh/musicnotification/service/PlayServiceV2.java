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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.LockActivityV2;
import com.xzh.musicnotification.notification.MusicNotificationV2;
import com.xzh.musicnotification.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.utils.UniLogUtils;

import static com.xzh.musicnotification.notification.MusicNotificationV2.NOTIFICATION_ID;

public class PlayServiceV2 extends Service implements MusicNotificationV2.NotificationHelperListener {
    private static PlayServiceV2 serviceV2;

    private boolean xzhFavour;
    private boolean Favour = false;
    private boolean Playing = false;

    private JSONObject songData;
    private ServiceBinder mBinder;
    private NotificationReceiver mReceiver;
    private WeakReference<AbsSDKInstance> mUniSDKInstance;

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

        ApplicationInfo info = Utils.getApplicationInfo(this);
        if (info != null) {
            xzhFavour = info.metaData.getBoolean("xzh_favour");
        }

        mReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
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
        stopForeground(true);
        MusicNotificationV2.getInstance().cancel();
        unregisterReceiver(mReceiver);
        UniLogUtils.i("XZH-musicNotification","serviceV2 消毁成功");
    }

    @Override
    public void onNotificationInit(Notification notification) {
        // 设置为前台Service
        Log.d("设置为前台Service", "onNotificationInit: " + notification);
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
                    serviceV2.mBinder.playOrPause(serviceV2.Playing);
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
                    serviceV2.mBinder.favour(!serviceV2.Favour);
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
        private WeakReference<OnClickListener> mClickListener;

        public void setActivity(OnClickListener clickListener){
            mClickListener = new WeakReference<>(clickListener);
        }

        public void setUniSDKInstance(AbsSDKInstance instance){
            mUniSDKInstance = new WeakReference<>(instance);
        }

        public void initNotification(JSONObject config) {
            MusicNotificationV2.getInstance().initNotification(serviceV2, config);
            UniLogUtils.i("XZH-musicNotification","创建通知栏成功");
            favour(Favour);
        }

        public boolean getFavour(){
            return serviceV2.Favour;
        }

        public boolean getPlaying(){
            return serviceV2.Playing;
        }

        public void lock(boolean locking) {
            mReceiver.lockActivity = locking;
        }

        public JSONObject getSongData() {
            return songData;
        }

        @SuppressLint("WrongConstant")
        public void playOrPause(boolean playing){
            Playing = playing;

            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().playOrPause(playing);

            Map<String, Object> options = new ArrayMap();
            options.put("playing", playing);
            PlayServiceV2.invoke(serviceV2,"playOrPause", options);

            MusicNotificationV2.getInstance().playOrPause(playing);
        }

        @SuppressLint("WrongConstant")
        public void favour(boolean isFavour){
            if (!xzhFavour) return;
            Favour = isFavour;
            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().favour(isFavour);

            Map<String, Object> options = new ArrayMap();
            options.put("favour", isFavour);
            PlayServiceV2.invoke(serviceV2,"favour", options);

            MusicNotificationV2.getInstance().favour(isFavour);
        }

        @SuppressLint("WrongConstant")
        public void update(JSONObject options){
            songData = options;
            Favour = options.getBoolean("favour");
            favour(Favour);

            if (mClickListener != null && mClickListener.get() != null) {
                mClickListener.get().update(options);
            }

            PlayServiceV2.invoke(serviceV2,"update", options);

            MusicNotificationV2.getInstance().updateSong(options);
        }

        public void fireGlobalEventCallback(String eventName, Map<String, Object> params){
            mUniSDKInstance.get().fireGlobalEventCallback(eventName, params);
        }
    }

    public static void invoke(Context context, String type, Map<String, Object> options) {
        try {
            Class<?> clazz = Class.forName("com.xzh.widget.MusicWidget");
            Method method = clazz.getMethod("update", Context.class, String.class, Map.class);
            method.invoke(null, context, type, options);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public interface OnClickListener {
        void update(JSONObject options);
        void favour(boolean favour);
        void playOrPause(boolean playing);
    }
}