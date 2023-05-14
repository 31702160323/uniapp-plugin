package com.xzh.musicnotification.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.transition.Transition;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.utils.PendingIntentInfo;
import com.xzh.musicnotification.utils.Utils;

public class MusicNotificationV2 extends BaseMusicNotification {
    private boolean systemStyle;
    private RemoteViews mRemoteViews; // 大布局
    private RemoteViews mSmallRemoteViews; //小布局

    public static MusicNotificationV2 getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final MusicNotificationV2 instance = new MusicNotificationV2();
    }

    public void createNotification() {
        cancel();
        Log.d("TAG", "createNotification: " + systemStyle);
        if (mConfig == null) return;
        mNotificationManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);

        String path = String.valueOf(mConfig.getString(Global.KEY_PATH));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) { //Android 5.1 以下
            mNotification = new Notification.Builder(mContext.get())
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setContent(getRemoteViews())
                    .setSmallIcon(R.drawable.music_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentIntent(getContentIntent(path))
                    .build();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //Android 8.0以下
            mNotification = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                    .setOngoing(true)
                    .setColorized(true)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setContent(getSmallRemoteViews())
                    .setSmallIcon(R.drawable.music_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setCustomBigContentView(getRemoteViews()) //展开视图
                    .setCustomContentView(getSmallRemoteViews())
                    .setContentIntent(getContentIntent(path))
                    .build();
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);

            if (!systemStyle) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
//                        .setOngoing(true)
//                        .setColorized(true)
//                        .setShowWhen(false)
//                        .setOnlyAlertOnce(true)
                        .setOngoing(false)
                        .setShowWhen(true)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.drawable.music_icon)
//                        .setBadgeIconType(R.drawable.music_icon)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(getContentIntent(path))
                        .setCustomBigContentView(getRemoteViews()) //展开视图
                        .setCustomContentView(getSmallRemoteViews());

                mNotification = builder.build();
            }
        }

        updateSong(songInfo);
        playOrPause(isPlay);

        // 设置为前台Service
        if(mNotification != null) ((Service) mContext.get()).startForeground(NOTIFICATION_ID, mNotification);
    }

    @Override
    protected void updateNotification() {
        if (mConfig == null) return;
        if (songInfo == null) return;
        if (mNotificationManager == null) createNotification();
        Utils.debounce(() -> {
            String picUrl = String.valueOf(songInfo.getString("picUrl"));
            String songName = String.valueOf(songInfo.getString(Global.KEY_SONG_NAME));
            String artistsName = String.valueOf(songInfo.getString(Global.KEY_ARTISTS_NAME));
            int play = isPlay ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white;
            int favour = songInfo.getBoolean(Global.KEY_FAVOUR) != null && songInfo.getBoolean(Global.KEY_FAVOUR) ? R.drawable.note_btn_loved : R.drawable.note_btn_love_white;
            if (systemStyle) {
                int dip64 = Utils.dip2px(64);
                generateGlide(mContext.get(), new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
//                                .setColorized(true)
                                .setOngoing(false)
                                .setShowWhen(true)
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.music_icon)
//                                .setBadgeIconType(R.drawable.music_icon)
                                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(getContentIntent(String.valueOf(mConfig.getString(Global.KEY_PATH))))
                                .setContentTitle(songName)
                                .setContentText(artistsName)
                                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                                .setLargeIcon(resource);

                        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
                        style.setMediaSession(mMediaSession.getSessionToken());
                        style.setShowCancelButton(true);

                        if (showFavour) {
                            style.setShowActionsInCompactView(1, 2, 3);
                            builder.addAction(generateAction(favour, "Favourite", 3, NotificationReceiver.EXTRA_FAV));
                        } else {
                            style.setShowActionsInCompactView(0, 1, 2);
                        }

                        builder.addAction(generateAction(R.drawable.note_btn_pre_white, "Previous", 1, NotificationReceiver.EXTRA_PRE));
                        builder.addAction(generateAction(play, "Play", 0, NotificationReceiver.EXTRA_PLAY));
                        builder.addAction(generateAction(R.drawable.note_btn_next_white, "Next", 2, NotificationReceiver.EXTRA_NEXT));
                        builder.setStyle(style);

                        mNotification = builder.build();
                        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                        ((Service) mContext.get()).startForeground(NOTIFICATION_ID, mNotification);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }
                }, picUrl, dip64, dip64);
            } else {
                if (showFavour) {
                    mRemoteViews.setImageViewResource(R.id.favourite_view, favour);
                }

                mRemoteViews.setTextViewText(R.id.title_view, songName);
                mRemoteViews.setTextViewText(R.id.tip_view, artistsName);
                mRemoteViews.setImageViewResource(R.id.play_view, play);
                setPicUrlBitmap(mContext.get(), mRemoteViews, picUrl, 112, 112);

                if (mSmallRemoteViews != null) {
                    mSmallRemoteViews.setTextViewText(R.id.tip_view, artistsName);
                    mSmallRemoteViews.setTextViewText(R.id.title_view, songName);
                    mSmallRemoteViews.setImageViewResource(R.id.play_view, play);
                    setPicUrlBitmap(mContext.get(), mSmallRemoteViews, picUrl, 64, 64);
                }
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }, 500);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private NotificationCompat.Action generateAction(int icon, CharSequence title, int requestCode, String EXTRA) {
        PendingIntent pendingIntent;
        Intent intent = new Intent(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
//        Intent intent = new Intent(mContext.get(), NotificationReceiver.class);
//        intent.setAction(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
        intent.setPackage(mContext.get().getPackageName());
        intent.putExtra(NotificationReceiver.EXTRA, EXTRA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(mContext.get(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(mContext.get(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private RemoteViews getRemoteViews() {
        if (mRemoteViews == null) {
            String packageName = mContext.get().getPackageName();
            mRemoteViews = new RemoteViews(packageName, R.layout.notification_big_layout);
            mRemoteViews.setTextViewText(R.id.title_view, "开启美好的一天");
            mRemoteViews.setImageViewResource(R.id.next_view, R.drawable.note_btn_next_white);
            mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
            mRemoteViews.setImageViewResource(R.id.previous_view, R.drawable.note_btn_pre_white);
            if (showFavour) {
                mRemoteViews.setViewVisibility(R.id.favourite_view, View.VISIBLE);
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
            }

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
                    //点击上一首按钮要发送的广播
                    new PendingIntentInfo(R.id.previous_view, 2, NotificationReceiver.EXTRA_PRE),
                    //点击下一首按钮要发送的广播
                    new PendingIntentInfo(R.id.next_view, 3, NotificationReceiver.EXTRA_NEXT)
            );
        }
        return mSmallRemoteViews;
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

    public void switchNotification(boolean is) {
        systemStyle = is;
        if (mNotificationManager != null) {
            createNotification();
        }
    }
}
