package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class PlayServiceV2 extends Service implements NotificationReceiver.IReceiverListener {
    private static PlayServiceV2 service;

    private boolean playing;
    private boolean lockActivity;

    private JSONObject songInfo;
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
        service = this;

        MusicNotificationV2.getInstance().initNotification(service);

        mReceiver = new NotificationReceiver(this);

        IntentFilter filter = new IntentFilter();
        // 锁屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 耳机广播
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        // 蓝牙广播
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        // 自定义广播
        filter.addAction(getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
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
        JSONObject data = new JSONObject();
        data.put("type", "destroy");
        fireGlobalEventCallback(Global.EVENT_MUSIC_LIFECYCLE, data);
        stopForeground(true);
        MusicNotificationV2.getInstance().cancel();
        unregisterReceiver(mReceiver);
        service = null;
    }

    @Override
    public void onScreenReceive() {
        try {
            if (lockActivity) Utils.openLock(PlayServiceV2.this, LockActivityV2.class);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
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
            case "enabled":
                if (songInfo != null) PlayServiceV2.invoke(service, Global.KEY_UPDATE, songInfo);

                Map<String, Object> options = new ArrayMap<>();
                options.put(Global.KEY_PLAYING, playing);
                PlayServiceV2.invoke(service,Global.KEY_PLAY_OR_PAUSE, options);
                break;
            default:
                data.put("message", "触发回调事件失败");
                data.put("code", -7);
                break;
        }
        if (mBinder != null) {
            mBinder.sendMessage(eventName, data);
        }
    }

    public void fireGlobalEventCallback(String eventName, Map<String, Object> params){
        if (mBinder != null) {
            mBinder.sendMessage(eventName, params);
        }
    }

    public class ServiceBinder extends Binder {
        private WeakReference<OnClickListener> mClickListener;
        private WeakReference<OnEventListener> mEventListener;

        public void setEventListener(OnEventListener eventListener){
            mEventListener = new WeakReference<>(eventListener);
        }

        public void setClickListener(OnClickListener clickListener){
            mClickListener = new WeakReference<>(clickListener);
        }

        public void switchNotification(boolean is) {
            MusicNotificationV2.getInstance().switchNotification(is);
        }

        public boolean getFavour(){
            return songInfo != null ? service.songInfo.getBoolean(Global.KEY_FAVOUR) : false;
        }

        public boolean getPlaying(){
            return service.playing;
        }

        public JSONObject getSongData() {
            return songInfo;
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
            PlayServiceV2.invoke(service,Global.KEY_PLAY_OR_PAUSE, options);

            MusicNotificationV2.getInstance().playOrPause(playing);
        }

        @SuppressLint("WrongConstant")
        public void favour(boolean favour){
            if (songInfo != null) {
                songInfo.put(Global.KEY_FAVOUR, favour);
                PlayServiceV2.invoke(service,Global.KEY_FAVOUR, songInfo);
            }

            if (mClickListener != null && mClickListener.get() != null) mClickListener.get().favour(favour);

            MusicNotificationV2.getInstance().favour(favour);
        }

        @SuppressLint("WrongConstant")
        public void update(JSONObject option){
            songInfo = option;

            if (mClickListener != null && mClickListener.get() != null) {
                mClickListener.get().update(option);
            }

            PlayServiceV2.invoke(service, Global.KEY_UPDATE, option);

            MusicNotificationV2.getInstance().updateSong(option);
        }

        public void sendMessage(String eventName, Map<String, Object> params){
            if (mEventListener != null && mEventListener.get() != null) mEventListener.get().sendMessage(eventName, params);
        }
    }

    public static void invoke(Context context, String type, Map<String, Object> options) {
        try {
            if (type == null) return;
            if (context == null) return;
            if (options == null) return;
            Class<?> clazz = Class.forName("com.xzh.widget.MusicWidget");
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

    public interface OnEventListener {
        void sendMessage(String eventName, Map<String, Object> params);
    }
}