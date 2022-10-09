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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.service.PlayServiceV3;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MusicNotificationV3 extends BaseMusicNotification {
    public void createNotification() {
        mNotificationManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);

        generateGlide(mContext.get(), new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                mNotificationManager.createNotificationChannel(notificationChannel);

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

                ((PlayServiceV3) mContext.get()).startForeground(mNotification);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
            }
        }, String.valueOf(songInfo.getString("picUrl")), WXViewUtils.dip2px(WXViewUtils.dip2px(64)), WXViewUtils.dip2px(WXViewUtils.dip2px(64)));
    }

    protected void buildNotification() {
        if (mNotificationManager == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification();
        }

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
}
