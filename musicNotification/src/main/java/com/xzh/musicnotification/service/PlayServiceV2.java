package com.xzh.musicnotification.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;
import com.xzh.musicnotification.LockActivityV2;
import com.xzh.musicnotification.notification.MusicNotificationV2;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import io.dcloud.feature.uniapp.bridge.UniJSCallback;

import static com.xzh.musicnotification.notification.MusicNotificationV2.NOTIFICATION_ID;

public class PlayServiceV2 extends Service implements MusicNotificationV2.NotificationHelperListener {
    private static PlayServiceV2 serviceV2;
    private JSONObject songData;
    private LockActivityV2 mActivityV2;
    private NotificationReceiver mReceiver;

    public boolean Favour = false;
    public boolean Playing = false;
    public Map<String, UniJSCallback> mCallback = new WeakHashMap<>();

    public static WeakReference<Intent> startMusicService(Context context) {
        Intent intent = new Intent(context, PlayServiceV2.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        return new WeakReference<>(intent);
    }

    public static void stopMusicService(Context context) {
        Intent intent = new Intent(context, PlayServiceV2.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicNotificationModule", "serviceV2 创建成功");
        serviceV2 = this;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MusicNotificationModule", "serviceV2 销毁成功");
        serviceV2 = null;
        unregisterReceiver(mReceiver);
        MusicNotificationV2.getInstance().cancel();
    }

    @Override
    public void onNotificationInit() {
        startForeground(NOTIFICATION_ID, MusicNotificationV2.getInstance().getNotification());
    }

    public void initNotification(JSONObject config) {
        MusicNotificationV2.getInstance().initNotification(this, config);
    }

    @SuppressLint("WrongConstant")
    public void update(JSONObject options){
        songData = options;
        Favour = options.getBoolean("favour");
        if (mActivityV2 != null) mActivityV2.updateUI(options);

        this.favour(Favour);
//        musicWidgetView.setTextViewText(R.id.title_view, options.getString("songName"));
//        musicWidgetView.setTextViewText(R.id.tip_view, options.getString("artistsName"));
//        musicWidgetView.setImageViewBitmap(R.id.image_view, ImageUtils.GetLocalOrNetBitmap(String.valueOf(options.getString("picUrl"))));
//        mAppWidgetManager.updateAppWidget(new ComponentName(this, MusicWidget.class), musicWidgetView);

        Intent intent = new Intent("com.xzh.widget.MusicWidget");
        intent.addFlags(0x01000000);
        intent.putExtra("type", "update");
        intent.putExtra("songName", options.getString("songName"));
        intent.putExtra("artistsName", options.getString("artistsName"));
        intent.putExtra("picUrl", options.getString("picUrl"));
        sendBroadcast(intent);

        MusicNotificationV2.getInstance().updateSong(options);
    }

    @SuppressLint("WrongConstant")
    public void playOrPause(boolean playing){
        Playing = playing;
        if (mActivityV2 != null) mActivityV2.playOrPause(playing);
//        if (playing) {
//            musicWidgetView.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
//        } else {
//            musicWidgetView.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
//        }
//        mAppWidgetManager.updateAppWidget(new ComponentName(this, MusicWidget.class), musicWidgetView);

        Intent intent = new Intent("com.xzh.widget.MusicWidget");
        intent.addFlags(0x01000000);
        intent.putExtra("type", "playOrPause");
        intent.putExtra("playing", playing);
        sendBroadcast(intent);

        MusicNotificationV2.getInstance().playOrPause(playing);
    }

    @SuppressLint("WrongConstant")
    public void favour(boolean isFavour){
        Favour = isFavour;
        if (mActivityV2 != null) mActivityV2.favour(isFavour);
//        if (isFavour) {
//            musicWidgetView.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
//        } else {
//            musicWidgetView.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
//        }
//        mAppWidgetManager.updateAppWidget(new ComponentName(this, MusicWidget.class), musicWidgetView);

        Log.d("FilePickerModule", "favour: " + isFavour);

        Intent intent = new Intent("com.xzh.widget.MusicWidget");
        intent.addFlags(0x01000000);
        intent.putExtra("type", "favour");
        intent.putExtra("favour", isFavour);
        sendBroadcast(intent);

        MusicNotificationV2.getInstance().favour(isFavour);
    }

    public void lock(boolean locking) {
        mReceiver.lockActivity = locking;
    }

    public void addCallback(String key, UniJSCallback callback){
        if (callback == null) return;
        mCallback.put(key, callback);
    }

    public JSONObject getSongData() {
        return songData;
    }

    public void setActivity(LockActivityV2 activityV2){
        mActivityV2 = activityV2;
    }

    /**
     * 接收Notification发送的广播
     */
    public static class NotificationReceiver extends BroadcastReceiver {
        public static final String ACTION_STATUS_BAR = "com.xzh.musicnotification.service.PlayServiceV2$NotificationReceiver.NOTIFICATION_ACTIONS";
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
            JSONObject data = new JSONObject();
            data.put("success", "操作成功");
            data.put("code", 0);
            JSCallback object = null;
            switch (extra) {
                case EXTRA_PLAY:
                    serviceV2.Playing = !serviceV2.Playing;
                    serviceV2.playOrPause(serviceV2.Playing);
                    if (serviceV2.mCallback.get(EXTRA_PLAY) != null) {
                        object = serviceV2.mCallback.get(EXTRA_PLAY);
                        break;
                    }
                    data.put("success", "操作失败");
                    data.put("code", -1);
                    break;
                case EXTRA_PRE:
                    if (serviceV2.mCallback.get(EXTRA_PRE) != null) {
                        object = serviceV2.mCallback.get(EXTRA_PRE);
                        break;
                    }
                    data.put("success", "操作失败");
                    data.put("code", -1);
                    break;
                case EXTRA_NEXT:
                    if (serviceV2.mCallback.get(EXTRA_NEXT) != null) {
                        object = serviceV2.mCallback.get(EXTRA_NEXT);
                        break;
                    }
                    data.put("success", "操作失败");
                    data.put("code", -1);
                    break;
                case EXTRA_FAV:
                    serviceV2.Favour = !serviceV2.Favour;
                    serviceV2.favour(serviceV2.Favour);
                    if (serviceV2.mCallback.get(EXTRA_FAV) != null) {
                        object = serviceV2.mCallback.get(EXTRA_FAV);
                        data.put("favourite", serviceV2.Favour);
                        break;
                    }
                    data.put("success", "操作失败");
                    data.put("code", -1);
                    break;
            }
            if (object != null) object.invokeAndKeepAlive(data);
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