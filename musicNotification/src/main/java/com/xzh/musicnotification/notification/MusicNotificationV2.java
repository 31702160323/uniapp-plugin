package com.xzh.musicnotification.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.ImageUtils;
import com.xzh.musicnotification.utils.PendingIntentInfo;

public class MusicNotificationV2 {
    public static final String CHANNEL_ID = "music_id_audio";
    public static final String CHANNEL_NAME = "music_name_audio";
    public static final int NOTIFICATION_ID = 0x111;

    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private RemoteViews mRemoteViews;
    private RemoteViews mSmallRemoteViews; //小布局
    private Context mContext;

    public static MusicNotificationV2 getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static MusicNotificationV2 instance = new MusicNotificationV2();
    }

    /*
     * 创建Notification,
     */
    public void initNotification(Object service, JSONObject config) {
        mContext = (Context) service;
        if (mNotification != null) return;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        initRemoteViews();

        Intent intent = new Intent("io.dcloud.PandoraEntry");
        intent.setClassName(mContext, "io.dcloud.PandoraEntryActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (config != null && config.get("path") != null) {
            intent.putExtra("path", config.getString("path"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            mNotification = new Notification.Builder(mContext)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.icon)
                    .setContent(mRemoteViews)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mNotification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.icon)
                    .setContent(mSmallRemoteViews)
                    .setCustomBigContentView(mRemoteViews) //展开视图
                    .setCustomContentView(mSmallRemoteViews)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder builder = new Notification.Builder(mContext, CHANNEL_ID)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setCustomBigContentView(mRemoteViews) //展开视图
                    .setCustomContentView(mSmallRemoteViews);

            if (config != null && config.get("icon") != null) {
                builder.setSmallIcon(Icon.createWithBitmap(ImageUtils.GetLocalOrNetBitmap(config.getString("icon"))));
            }

            mNotification = builder.build();
        }
        //数据
        ((NotificationHelperListener) service).onNotificationInit();
    }

    /**
     * 创建Notification的布局,默认布局为Loading状态
     */
    private void initRemoteViews() {
        String packageName = mContext.getPackageName();

        mSmallRemoteViews = new RemoteViews(packageName, R.layout.notification_small_layout);
        mSmallRemoteViews.setTextViewText(R.id.title_view, "songName");
        mSmallRemoteViews.setTextViewText(R.id.tip_view, "artistsName");
        mSmallRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        mRemoteViews = new RemoteViews(packageName, R.layout.notification_big_layout);
        mRemoteViews.setTextViewText(R.id.title_view, "songName");
        mRemoteViews.setTextViewText(R.id.tip_view, "artistsName");

        mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
        mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
        mRemoteViews.setImageViewResource(R.id.previous_view, R.mipmap.note_btn_pre_white);
        mRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        PendingIntentInfo.addOnClickPendingIntents(mRemoteViews, mContext,
                //点击播放按钮要发送的广播
                new PendingIntentInfo(R.id.play_view, 1,PlayServiceV2.NotificationReceiver.EXTRA_PLAY),
                //点击上一首按钮要发送的广播
                new PendingIntentInfo(R.id.previous_view, 2,PlayServiceV2.NotificationReceiver.EXTRA_PRE),
                //点击下一首按钮要发送的广播
                new PendingIntentInfo(R.id.next_view, 3,PlayServiceV2.NotificationReceiver.EXTRA_NEXT),
                //点击收藏按钮要发送的广播
                new PendingIntentInfo(R.id.favourite_view, 4,PlayServiceV2.NotificationReceiver.EXTRA_FAV)
        );

        PendingIntentInfo.addOnClickPendingIntents(mSmallRemoteViews, mContext,
                //点击播放按钮要发送的广播
                new PendingIntentInfo(R.id.play_view, 1,PlayServiceV2.NotificationReceiver.EXTRA_PLAY),
                //点击下一首按钮要发送的广播
                new PendingIntentInfo(R.id.next_view, 3,PlayServiceV2.NotificationReceiver.EXTRA_NEXT)
        );
    }

    /**
     * 更新 Notification 信息
     *
     * @param options 歌曲信息
     */
    public void updateSong(JSONObject options) {
        if (options.getString("songName") != null) {
            mRemoteViews.setTextViewText(R.id.title_view, String.valueOf(options.getString("songName")));
            mSmallRemoteViews.setTextViewText(R.id.title_view, String.valueOf(options.getString("songName")));
        }
        if (options.getString("artistsName") != null) {
            mRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(options.getString("artistsName")));
            mSmallRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(options.getString("artistsName")));
        }
        if (options.getBoolean("favour") != null && options.getBoolean("favour")) {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
        } else {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
        }
        Bitmap bitmap = ImageUtils.GetLocalOrNetBitmap(String.valueOf(options.getString("picUrl")));
        if (bitmap != null) {
            mRemoteViews.setImageViewBitmap(R.id.image_view, bitmap);
            mSmallRemoteViews.setImageViewBitmap(R.id.image_view, bitmap);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * 切换播放状态
     *
     * @param isPlay 播放状态
     */
    public void playOrPause(boolean isPlay) {
        if (isPlay) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
        } else {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * 切换搜藏状态
     *
     * @param favourite 搜藏状态
     */
    public void favour(boolean favourite) {
        if (favourite) {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
        } else {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void cancel() {
        if(mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public Notification getNotification() {
        return mNotification;
    }

    /**
     * 与音乐service的回调通信
     */
    public interface NotificationHelperListener {
        void onNotificationInit();
    }
}
