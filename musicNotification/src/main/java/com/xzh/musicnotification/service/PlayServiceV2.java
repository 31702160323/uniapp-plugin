package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
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
import com.xzh.musicnotification.LockActivityV3;
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

public class PlayServiceV2 extends Service implements MusicNotificationV2.NotificationHelperListener, NotificationReceiver.IReceiverListener {
    private static PlayServiceV2 serviceV2;

    private boolean xzhFavour;
    private boolean Favour = false;
    private boolean Playing = false;
    private boolean lockActivity = false;

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

        mReceiver = new NotificationReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
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

    @Override
    public void onReceive(String action, String extra) {
        Log.d("NotificationReceiver", "onCreate: " + extra);
        if (lockActivity && Intent.ACTION_SCREEN_OFF.equals(action)) {
            Utils.openLock(this, LockActivityV2.class);
        }
        if (extra == null) return;
        String eventName = "musicNotificationError";
        Map<String, Object> data = new HashMap<>();
        data.put("message", "触发回调事件成功");
        data.put("code", 0);
        switch (extra) {
            case NotificationReceiver.EXTRA_PLAY:
                mBinder.playOrPause(serviceV2.Playing);
                UniLogUtils.i("XZH-musicNotification","点击播放按钮");
                eventName = "musicNotificationPause";
                break;
            case NotificationReceiver.EXTRA_PRE:
                UniLogUtils.i("XZH-musicNotification","点击上一首按钮");
                eventName = "musicNotificationPrevious";
                break;
            case NotificationReceiver.EXTRA_NEXT:
                UniLogUtils.i("XZH-musicNotification","点击下一首按钮");
                eventName = "musicNotificationNext";
                break;
            case NotificationReceiver.EXTRA_FAV:
                mBinder.favour(!Favour);
                UniLogUtils.i("XZH-musicNotification","点击搜藏按钮");
                eventName = "musicNotificationFavourite";
                break;
            default:
                data.put("message", "触发回调事件失败");
                data.put("code", -7);
                break;
        }
        mBinder.fireGlobalEventCallback(eventName, data);
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

        public JSONObject getSongData() {
            return songData;
        }

        public void lock(boolean locking) {
            lockActivity = locking;
        }

        @SuppressLint("WrongConstant")
        public void playOrPause(boolean playing){
            Playing = playing;

            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().playOrPause(playing);

            Map<String, Object> options = new ArrayMap<>();
            options.put("playing", playing);
            PlayServiceV2.invoke(serviceV2,"playOrPause", options);

            MusicNotificationV2.getInstance().playOrPause(playing);
        }

        @SuppressLint("WrongConstant")
        public void favour(boolean isFavour){
            if (!xzhFavour) return;
            Favour = isFavour;
            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().favour(isFavour);

            Map<String, Object> options = new ArrayMap<>();
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
            Method method = clazz.getDeclaredMethod("invoke", Context.class, String.class, Map.class);
            method.invoke(clazz, context, type, options);
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