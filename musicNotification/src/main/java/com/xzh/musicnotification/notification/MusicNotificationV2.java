package com.xzh.musicnotification.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.PendingIntentInfo;

import java.lang.ref.WeakReference;

import io.dcloud.PandoraEntryActivity;

public class MusicNotificationV2 {
    public static final int NOTIFICATION_ID = 0x111;
    public static final String CHANNEL_ID = "music_id_audio";
    public static final String CHANNEL_NAME = "music_name_audio";

    private JSONObject songInfo;
    private boolean isPlay;
    private boolean systemStyle;
    private JSONObject mConfig;
    private RemoteViews mRemoteViews; // 大布局
    private Notification mNotification;
    private RemoteViews mSmallRemoteViews; //小布局
    private WeakReference<Context> mContext;
    private NotificationManager mNotificationManager;
    private MediaSessionCompat mediaSession;
    private CustomTarget<Bitmap> mTarget;
    private Bitmap mIcon;

    public static MusicNotificationV2 getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final MusicNotificationV2 instance = new MusicNotificationV2();
    }

    private PendingIntent getContentIntent(String path) {
        Intent intent = new Intent(mContext.get(), PandoraEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (path != null) {
            intent.putExtra("path", path);
        }
        return PendingIntent.getActivity(mContext.get(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 创建Notification,
     *
     * @param service MusicNotificationV2.NotificationHelperListener
     * @param config  JSONObject
     */
    public void initNotification(Context service, JSONObject config) {
        mContext = new WeakReference<>(service);
        mConfig = config;
        mediaSession = new MediaSessionCompat(mContext.get(), CHANNEL_ID);
        mediaSession.setActive(true);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().build());
    }

    public void createNotification() {
        mNotificationManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) { //Android 5.1 以下
            mNotification = new Notification.Builder(mContext.get())
                    .setOngoing(true)
                    .setContent(getRemoteViews())
                    .setSmallIcon(R.drawable.music_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentIntent(getContentIntent(mConfig.getString("path")))
                    .build();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //Android 8.0以下
            mNotification = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                    .setOngoing(true)
                    .setContent(getSmallRemoteViews())
                    .setSmallIcon(R.drawable.music_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setCustomBigContentView(getRemoteViews()) //展开视图
                    .setCustomContentView(getSmallRemoteViews())
                    .setContentIntent(getContentIntent(mConfig.getString("path")))
                    .build();
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);

            if (systemStyle) {
                mIcon = BitmapFactory.decodeResource(mContext.get().getResources(), R.drawable.music_icon);
                buildNotification();
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                        .setOngoing(true)
                        .setShowWhen(false)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.drawable.music_icon)
                        .setBadgeIconType(R.drawable.music_icon)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(getContentIntent(mConfig.getString("path")))
                        .setCustomBigContentView(getRemoteViews()) //展开视图
                        .setCustomContentView(getSmallRemoteViews());

                mNotification = builder.build();

                if (songInfo != null) {
                    updateSong(songInfo);
                    playOrPause(isPlay);
                }
            }
        }

        ((PlayServiceV2) mContext.get()).startForeground(mNotification);
    }

    private void buildNotification() {
        buildNotification(mIcon);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        mTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                mIcon = resource;
                buildNotification(resource);
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
            }
        };
        generateGlide(mContext.get(), mTarget, String.valueOf(songInfo.getString("picUrl")), WXViewUtils.dip2px(WXViewUtils.dip2px(64)), WXViewUtils.dip2px(WXViewUtils.dip2px(64)));
    }

    private void buildNotification(Bitmap icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                .setOngoing(true)
                .setColorized(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.music_icon)
                .setBadgeIconType(R.drawable.music_icon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(getContentIntent(mConfig.getString("path")))
                .setContentTitle(String.valueOf(songInfo.getString("songName")))
                .setContentText(String.valueOf(songInfo.getString("artistsName")))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setLargeIcon(icon)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(1, 2).setShowCancelButton(true).setMediaSession(mediaSession.getSessionToken()));

        builder.addAction(generateAction(R.drawable.note_btn_pre_white, "Previous", 2, NotificationReceiver.EXTRA_PRE));

        if (isPlay) {
            builder.addAction(generateAction(R.drawable.note_btn_pause_white, "Play", 1, NotificationReceiver.EXTRA_PLAY));
        } else {
            builder.addAction(generateAction(R.drawable.note_btn_play_white, "Play", 1, NotificationReceiver.EXTRA_PLAY));
        }

        builder.addAction(generateAction(R.drawable.note_btn_next_white, "Next", 3, NotificationReceiver.EXTRA_NEXT));

        mNotification = builder.build();
    }


    private NotificationCompat.Action generateAction(int icon, CharSequence title, int requestCode, String EXTRA) {
        return new NotificationCompat.Action(icon, title, PendingIntent.getBroadcast(mContext.get(), requestCode,
                new Intent(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR)
                        .putExtra(NotificationReceiver.EXTRA, EXTRA),
                PendingIntent.FLAG_UPDATE_CURRENT
        ));
    }

    private RemoteViews getRemoteViews() {
        if (mRemoteViews == null) {
            String packageName = mContext.get().getPackageName();
            mRemoteViews = new RemoteViews(packageName, R.layout.notification_big_layout);
            mRemoteViews.setTextViewText(R.id.title_view, "开启美好的一天");
            mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
            mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
            mRemoteViews.setImageViewResource(R.id.previous_view, R.drawable.note_btn_pre_white);
            mRemoteViews.setImageViewResource(R.id.next_view, R.drawable.note_btn_next_white);

            PendingIntentInfo.addOnClickPendingIntents(mRemoteViews, mContext.get(),
                    //点击播放按钮要发送的广播
                    new PendingIntentInfo(R.id.play_view, 1, NotificationReceiver.EXTRA_PLAY),
                    //点击上一首按钮要发送的广播
                    new PendingIntentInfo(R.id.previous_view, 2, NotificationReceiver.EXTRA_PRE),
                    //点击下一首按钮要发送的广播
                    new PendingIntentInfo(R.id.next_view, 3, NotificationReceiver.EXTRA_NEXT),
                    //点击收藏按钮要发送的广播
                    new PendingIntentInfo(R.id.favourite_view, 4, NotificationReceiver.EXTRA_FAV)
            );
        }
        return mRemoteViews;
    }

    private RemoteViews getSmallRemoteViews() {
        if (mSmallRemoteViews == null) {
            String packageName = mContext.get().getPackageName();
            mSmallRemoteViews = new RemoteViews(packageName, R.layout.notification_small_layout);
            mSmallRemoteViews.setTextViewText(R.id.title_view, "开启美好的一天");

            mSmallRemoteViews.setImageViewResource(R.id.previous_view, R.drawable.note_btn_pre_white);
            mSmallRemoteViews.setImageViewResource(R.id.next_view, R.drawable.note_btn_next_white);

            PendingIntentInfo.addOnClickPendingIntents(mSmallRemoteViews, mContext.get(),
                    //点击播放按钮要发送的广播
                    new PendingIntentInfo(R.id.play_view, 1, NotificationReceiver.EXTRA_PLAY),
                    //点击下一首按钮要发送的广播
                    new PendingIntentInfo(R.id.next_view, 3, NotificationReceiver.EXTRA_NEXT)
            );
        }
        return mSmallRemoteViews;
    }

    /**
     * 更新 Notification 信息
     *
     * @param options 歌曲信息
     */
    public void updateSong(JSONObject options) {
        songInfo = options;
        if (mNotificationManager == null) {
            createNotification();
        }
        if (mRemoteViews != null) {
            if (options.getString("songName") != null) {
                mRemoteViews.setTextViewText(R.id.title_view, String.valueOf(options.getString("songName")));
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setTextViewText(R.id.title_view, String.valueOf(options.getString("songName")));
            }
            if (options.getString("artistsName") != null) {
                mRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(options.getString("artistsName")));
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(options.getString("artistsName")));
            }
            if (options.getBoolean("favour") != null && options.getBoolean("favour")) {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_loved);
            } else {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
            }

            setPicUrlBitmap(mContext.get(), mRemoteViews, String.valueOf(options.getString("picUrl")), WXViewUtils.dip2px(112), WXViewUtils.dip2px(112));
            if (mSmallRemoteViews != null) {
                setPicUrlBitmap(mContext.get(), mSmallRemoteViews, String.valueOf(options.getString("picUrl")), WXViewUtils.dip2px(64), WXViewUtils.dip2px(64));
            }

            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        } else {
            Log.d("TAG", "updateSong: ");
            buildNotification();
        }
    }

    private void setPicUrlBitmap(Context context, RemoteViews remoteViews, String picUrl, float width, float height) {
        NotificationTarget target = new NotificationTarget(
                context,
                R.id.image_view,
                remoteViews,
                mNotification,
                NOTIFICATION_ID);

        generateGlide(context, target, picUrl, width, height);
    }

    private void generateGlide(Context context, Target<Bitmap> target, String picUrl, float width, float height) {
        Glide.with(context)
                .asBitmap()
                .load(picUrl)
                .sizeMultiplier(0.8f)
                .format(DecodeFormat.PREFER_RGB_565)
                .override(WXViewUtils.dip2px(width), WXViewUtils.dip2px(height))
                .into(target);
    }

    /**
     * 切换播放状态
     *
     * @param isPlay 播放状态
     */
    public void playOrPause(boolean isPlay) {
        this.isPlay = isPlay;
        if (mRemoteViews != null) {
            if (isPlay) {
                mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_pause_white);
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_pause_white);
            } else {
                mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
            }
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        } else {
            buildNotification();
        }
    }

    /**
     * 切换搜藏状态
     *
     * @param favourite 搜藏状态
     */
    public void favour(boolean favourite) {
        if (mRemoteViews != null) {
            mRemoteViews.setViewVisibility(R.id.favourite_view, View.VISIBLE);
            if (favourite) {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_loved);
            } else {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
            }
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void switchNotification(boolean is) {
        systemStyle = is;
        if (mNotificationManager != null) {
            cancel();
            createNotification();
        }
    }

    public void cancel() {
        mRemoteViews = null;
        mSmallRemoteViews = null;
        if (mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
