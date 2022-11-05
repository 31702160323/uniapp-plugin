package com.xzh.musicnotification.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.transition.Transition;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.PendingIntentInfo;

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
        mNotificationManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) { //Android 5.1 以下
            mNotification = new Notification.Builder(mContext.get())
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setContent(getRemoteViews())
                    .setSmallIcon(R.drawable.music_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentIntent(getContentIntent(mConfig.getString(Global.KEY_PATH)))
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
                    .setContentIntent(getContentIntent(mConfig.getString(Global.KEY_PATH)))
                    .build();
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);

            if (systemStyle) {
                buildNotification();
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                        .setOngoing(true)
                        .setColorized(true)
                        .setShowWhen(false)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.drawable.music_icon)
//                        .setBadgeIconType(R.drawable.music_icon)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(getContentIntent(mConfig.getString(Global.KEY_PATH)))
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

    @Override
    protected void buildNotification() {
        if (mNotificationManager == null) {
            createNotification();
        }
        if (systemStyle) {
            generateGlide(mContext.get(), new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.get(), CHANNEL_ID)
                            .setOngoing(true)
                            .setColorized(true)
                            .setShowWhen(false)
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.drawable.music_icon)
//                .setBadgeIconType(R.drawable.music_icon)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentIntent(getContentIntent(mConfig.getString(Global.KEY_PATH)))
                            .setContentTitle(String.valueOf(songInfo.getString(Global.KEY_SONG_NAME)))
                            .setContentText(String.valueOf(songInfo.getString(Global.KEY_ARTISTS_NAME)))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                            .setLargeIcon(resource);

                    androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
                    style.setMediaSession(mMediaSession.getSessionToken());
                    style.setShowCancelButton(true);

                    if (showFavour) {
                        style.setShowActionsInCompactView(1, 2, 3);
                        if (songInfo.getBoolean(Global.KEY_FAVOUR)) {
                            builder.addAction(generateAction(R.drawable.note_btn_loved, "Favourite", 4, NotificationReceiver.EXTRA_FAV));
                        } else {
                            builder.addAction(generateAction(R.drawable.note_btn_love_white, "Favourite", 4, NotificationReceiver.EXTRA_FAV));
                        }
                    } else {
                        style.setShowActionsInCompactView(0, 1, 2);
                    }

                    builder.addAction(generateAction(R.drawable.note_btn_pre_white, "Previous", 2, NotificationReceiver.EXTRA_PRE));

                    if (isPlay) {
                        builder.addAction(generateAction(R.drawable.note_btn_pause_white, "Play", 1, NotificationReceiver.EXTRA_PLAY));
                    } else {
                        builder.addAction(generateAction(R.drawable.note_btn_play_white, "Play", 1, NotificationReceiver.EXTRA_PLAY));
                    }

                    builder.addAction(generateAction(R.drawable.note_btn_next_white, "Next", 3, NotificationReceiver.EXTRA_NEXT));

                    builder.setStyle(style);

                    mNotification = builder.build();
                    mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                }
            }, String.valueOf(songInfo.getString("picUrl")), WXViewUtils.dip2px(WXViewUtils.dip2px(64)), WXViewUtils.dip2px(WXViewUtils.dip2px(64)));
        } else {
            if (isPlay) {
                mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_pause_white);
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_pause_white);
            } else {
                mRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
            }

            if (songInfo.getString(Global.KEY_SONG_NAME) != null) {
                mRemoteViews.setTextViewText(R.id.title_view, String.valueOf(songInfo.getString(Global.KEY_SONG_NAME)));
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setTextViewText(R.id.title_view, String.valueOf(songInfo.getString(Global.KEY_SONG_NAME)));
            }
            if (songInfo.getString(Global.KEY_ARTISTS_NAME) != null) {
                mRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(songInfo.getString(Global.KEY_ARTISTS_NAME)));
                if (mSmallRemoteViews != null)
                    mSmallRemoteViews.setTextViewText(R.id.tip_view, String.valueOf(songInfo.getString(Global.KEY_ARTISTS_NAME)));
            }

            if (songInfo.getBoolean(Global.KEY_FAVOUR)) {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_loved);
            } else {
                mRemoteViews.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
            }

            setPicUrlBitmap(mContext.get(), mRemoteViews, String.valueOf(songInfo.getString("picUrl")), WXViewUtils.dip2px(112), WXViewUtils.dip2px(112));
            if (mSmallRemoteViews != null) {
                setPicUrlBitmap(mContext.get(), mSmallRemoteViews, String.valueOf(songInfo.getString("picUrl")), WXViewUtils.dip2px(64), WXViewUtils.dip2px(64));
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private NotificationCompat.Action generateAction(int icon, CharSequence title, int requestCode, String EXTRA) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new NotificationCompat.Action(icon, title, PendingIntent.getBroadcast(mContext.get(), requestCode,
                    new Intent(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR)
                            .putExtra(NotificationReceiver.EXTRA, EXTRA),
                    PendingIntent.FLAG_IMMUTABLE
            ));
        } else {
            return new NotificationCompat.Action(icon, title, PendingIntent.getBroadcast(mContext.get(), requestCode,
                    new Intent(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR)
                            .putExtra(NotificationReceiver.EXTRA, EXTRA),
                    PendingIntent.FLAG_UPDATE_CURRENT
            ));
        }
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
            cancel();
            createNotification();
        }
    }
}
