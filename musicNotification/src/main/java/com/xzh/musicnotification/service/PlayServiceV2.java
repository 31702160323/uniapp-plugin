package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.ArrayMap;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.LockActivityV2;
import com.xzh.musicnotification.notification.MusicNotificationV2;
import com.xzh.musicnotification.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;

import io.dcloud.feature.uniapp.AbsSDKInstance;

import static com.xzh.musicnotification.notification.MusicNotificationV2.NOTIFICATION_ID;

public class PlayServiceV2 extends Service {
    private static PlayServiceV2 serviceV2;

    private boolean showFavour;
    private boolean playing;
    private boolean lockActivity;

    private JSONObject songData;
    private ServiceBinder mBinder;
    private NotificationReceiver mReceiver;

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
        context.stopService(new Intent(context, PlayServiceV2.class));
    }

    @Override
    @SuppressLint("WrongConstant")
    public void onCreate() {
        super.onCreate();
        serviceV2 = this;

        ApplicationInfo info = Utils.getApplicationInfo(this);
        if (info != null) {
            showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR);
        }

        mReceiver = new NotificationReceiver(new NotificationReceiver.IReceiverListener() {
            @Override
            public void onScreenReceive() {
                if (lockActivity) Utils.openLock(PlayServiceV2.this, LockActivityV2.class);
            }

            @Override
            public void onHeadsetReceive(int extra) {
                JSONObject data = new JSONObject();
                data.put("type", Global.MEDIA_BUTTON_HEADSET);
                data.put("keyCode", extra);
                fireGlobalEventCallback(Global.EVENT_MUSIC_MEDIA_BUTTON, data);
            }

            @Override
            public void onBluetoothReceive(int extra) {
                JSONObject data = new JSONObject();
                data.put("type", Global.MEDIA_BUTTON_BLUETOOTH);
                data.put("keyCode", extra);
                fireGlobalEventCallback(Global.EVENT_MUSIC_MEDIA_BUTTON, data);
            }

            @Override
            public void onMusicReceive(String extra) {
                String eventName = Global.EVENT_MUSIC_NOTIFICATION_ERROR;
                JSONObject data = new JSONObject();
                data.put("message", "触发回调事件成功");
                data.put("code", 0);
                switch (extra) {
                    case NotificationReceiver.EXTRA_PLAY:
                        eventName = Global.EVENT_MUSIC_NOTIFICATION_PAUSE;
                        break;
                    case NotificationReceiver.EXTRA_PRE:
                        eventName = Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS;
                        break;
                    case NotificationReceiver.EXTRA_NEXT:
                        eventName = Global.EVENT_MUSIC_NOTIFICATION_NEXT;
                        break;
                    case NotificationReceiver.EXTRA_FAV:
                        eventName = Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE;
                        break;
                    default:
                        data.put("message", "触发回调事件失败");
                        data.put("code", -7);
                        break;
                }
                if (mBinder != null) {
                    mBinder.fireGlobalEventCallback(eventName, data);
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        // 锁屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 耳机广播
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        // 蓝牙广播
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        // 自定义广播
        filter.addAction(getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
        registerReceiver(mReceiver, filter);
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
    }

    public final void startForeground(Notification notification) {
        // 设置为前台Service
        startForeground(NOTIFICATION_ID, notification);
    }

    public void fireGlobalEventCallback(String eventName, Map<String, Object> params){
        mBinder.fireGlobalEventCallback(eventName, params);
    }

    public class ServiceBinder extends Binder {
        private WeakReference<OnClickListener> mClickListener;
        private WeakReference<AbsSDKInstance> mUniSDKInstance;

        public void setActivity(OnClickListener clickListener){
            mClickListener = new WeakReference<>(clickListener);
        }

        public void setUniSDKInstance(AbsSDKInstance instance){
            mUniSDKInstance = new WeakReference<>(instance);
        }

        public void initNotification(JSONObject config) {
            MusicNotificationV2.getInstance().initNotification(serviceV2, config);
        }

        public void switchNotification(boolean is) {
            MusicNotificationV2.getInstance().switchNotification(is);
        }

        public boolean getFavour(){
            return serviceV2.songData.getBoolean(Global.KEY_FAVOUR);
        }

        public boolean getPlaying(){
            return serviceV2.playing;
        }

        public JSONObject getSongData() {
            return songData;
        }

        public void lock(boolean locking) {
            lockActivity = locking;
        }

        @SuppressLint("WrongConstant")
        public void playOrPause(boolean playing){
            PlayServiceV2.this.playing = playing;

            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().playOrPause(playing);

            Map<String, Object> options = new ArrayMap<>();
            options.put(Global.KEY_PLAYING, playing);
            PlayServiceV2.invoke(serviceV2,"playOrPause", options);

            MusicNotificationV2.getInstance().playOrPause(playing);
        }

        @SuppressLint("WrongConstant")
        public void favour(boolean isFavour){
            if (!showFavour) return;
            songData.put(Global.KEY_FAVOUR, isFavour);

            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().favour(isFavour);

            PlayServiceV2.invoke(serviceV2,Global.KEY_FAVOUR, songData);

            MusicNotificationV2.getInstance().favour(isFavour);
        }

        @SuppressLint("WrongConstant")
        public void update(JSONObject options){
            songData = options;
            if (options.getBoolean(Global.KEY_FAVOUR) != null) {
                songData.put(Global.KEY_FAVOUR, options.getBoolean(Global.KEY_FAVOUR));
            }

            if (mClickListener != null && mClickListener.get() != null) {
                mClickListener.get().update(options);
            }

            PlayServiceV2.invoke(serviceV2,"update", options);

            MusicNotificationV2.getInstance().updateSong(options);
        }

        public void fireGlobalEventCallback(String eventName, Map<String, Object> params){
            if (mUniSDKInstance != null && mUniSDKInstance.get() != null) mUniSDKInstance.get().fireGlobalEventCallback(eventName, params);
        }
    }

    public static void invoke(Context context, String type, Map<String, Object> options) {
        try {
            Class<?> clazz = Class.forName("com.xzh.widget.MusicWidget");
            if (options == null) return;
            Method method = clazz.getDeclaredMethod("invoke", Context.class, String.class, Map.class);
            method.invoke(clazz, context, type, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnClickListener {
        void update(JSONObject options);
        void favour(boolean favour);
        void playOrPause(boolean playing);
    }
}