package com.xzh.musicnotification.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.target.NotificationTarget;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.service.PlayServiceV3;
import com.xzh.musicnotification.utils.PendingIntentInfo;

public class MusicNotificationSystem extends BaseMusicNotification {
    private RemoteViews mRemoteViews; // 大布局
    private RemoteViews mSmallRemoteViews; //小布局

    @Override
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

        ((PlayServiceV3) mContext.get()).startForeground(mNotification);
    }

    @Override
    protected void buildNotification() {
        if (mNotificationManager == null) {
            createNotification();
        }

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

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
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
}
