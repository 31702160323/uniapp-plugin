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
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.NotificationTarget;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.ImageUtils;
import com.xzh.musicnotification.utils.PendingIntentInfo;

import java.lang.ref.WeakReference;

public class MusicNotificationV2 {
    public static final String CHANNEL_ID = "music_id_audio";
    public static final String CHANNEL_NAME = "music_name_audio";
    public static final int NOTIFICATION_ID = 0x111;

    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private RemoteViews mRemoteViews; // 大布局
    private RemoteViews mSmallRemoteViews; //小布局
    private WeakReference<Context> mContext;

    public static MusicNotificationV2 getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final MusicNotificationV2 instance = new MusicNotificationV2();
    }

    /*
     * 创建Notification,
     */
    public void initNotification(PlayServiceV2 service, JSONObject config) {
        mContext = new WeakReference<>(service);
        if (mNotification != null) return;

        mNotificationManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);
        initRemoteViews();

        Intent intent = new Intent("io.dcloud.PandoraEntry");
        intent.setClassName(mContext.get(), "io.dcloud.PandoraEntryActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (config != null && config.get("path") != null) {
            intent.putExtra("path", config.getString("path"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext.get(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            mNotification = new Notification.Builder(mContext.get())
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.icon)
                    .setContent(mRemoteViews)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mNotification = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
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

            Notification.Builder builder = new Notification.Builder(mContext.get(), CHANNEL_ID)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setCustomBigContentView(mRemoteViews) //展开视图
                    .setCustomContentView(mSmallRemoteViews);

            if (config != null && config.get("icon") != null) {
                Bitmap bitmap = ImageUtils.GetLocalOrNetBitmap(config.getString("icon"));
                if (bitmap != null) {
                    builder.setSmallIcon(Icon.createWithBitmap(bitmap));
                }

//                Glide.with(service)
//                        .asBitmap()
//                        .sizeMultiplier(0.8f)
//                        .override(40, 40)
//                        .load(config.getString("icon"))
//                        .into(new CustomTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                                builder.setSmallIcon(Icon.createWithBitmap(resource));
//                            }
//
//                            @Override
//                            public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                            }
//                        });
            }

            mNotification = builder.build();
        }
        //数据
        ((NotificationHelperListener) service).onNotificationInit(mNotification);
    }

    /**
     * 创建Notification的布局,默认布局为Loading状态
     */
    private void initRemoteViews() {
        String packageName = mContext.get().getPackageName();

        mSmallRemoteViews = new RemoteViews(packageName, R.layout.notification_small_layout);
        mSmallRemoteViews.setTextViewText(R.id.title_view, "开启美好的一天");

        mRemoteViews = new RemoteViews(packageName, R.layout.notification_big_layout);
        mRemoteViews.setTextViewText(R.id.title_view, "开启美好的一天");

        mSmallRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
        mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
        mRemoteViews.setImageViewResource(R.id.previous_view, R.mipmap.note_btn_pre_white);
        mRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        PendingIntentInfo.addOnClickPendingIntents(mRemoteViews, mContext.get(),
                //点击播放按钮要发送的广播
                new PendingIntentInfo(R.id.play_view, 1, PlayServiceV2.NotificationReceiver.EXTRA_PLAY),
                //点击上一首按钮要发送的广播
                new PendingIntentInfo(R.id.previous_view, 2, PlayServiceV2.NotificationReceiver.EXTRA_PRE),
                //点击下一首按钮要发送的广播
                new PendingIntentInfo(R.id.next_view, 3, PlayServiceV2.NotificationReceiver.EXTRA_NEXT),
                //点击收藏按钮要发送的广播
                new PendingIntentInfo(R.id.favourite_view, 4, PlayServiceV2.NotificationReceiver.EXTRA_FAV)
        );

        PendingIntentInfo.addOnClickPendingIntents(mSmallRemoteViews, mContext.get(),
                //点击播放按钮要发送的广播
                new PendingIntentInfo(R.id.play_view, 1, PlayServiceV2.NotificationReceiver.EXTRA_PLAY),
                //点击下一首按钮要发送的广播
                new PendingIntentInfo(R.id.next_view, 3, PlayServiceV2.NotificationReceiver.EXTRA_NEXT)
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

        NotificationTarget targetRemote = new NotificationTarget(
                mContext.get(),
                R.id.image_view,
                mRemoteViews,
                mNotification,
                NOTIFICATION_ID);

        NotificationTarget targetSmall = new NotificationTarget(
                mContext.get(),
                R.id.image_view,
                mSmallRemoteViews,
                mNotification,
                NOTIFICATION_ID);

        Glide.with(mContext.get()) // safer!
                .asBitmap()
                .load(String.valueOf(options.getString("picUrl")))
                .sizeMultiplier(0.8f)
                .format(DecodeFormat.PREFER_RGB_565)
                .override(WXViewUtils.dip2px(112), WXViewUtils.dip2px(112))
                .into(targetRemote);

        Glide.with(mContext.get()) // safer!
                .asBitmap()
                .load(String.valueOf(options.getString("picUrl")))
                .sizeMultiplier(0.8f)
                .format(DecodeFormat.PREFER_RGB_565)
                .override(WXViewUtils.dip2px(64), WXViewUtils.dip2px(64))
                .into(targetSmall);
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
        mRemoteViews.setViewVisibility(R.id.favourite_view, View.VISIBLE);
        if (favourite) {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
        } else {
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void cancel() {
        if (mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * 与音乐service的回调通信
     */
    public interface NotificationHelperListener {
        void onNotificationInit(Notification notification);
    }
}
