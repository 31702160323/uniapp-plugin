package com.xzh.musicnotification.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.Target;
import com.taobao.weex.utils.WXViewUtils;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.service.PlayServiceV2;

import java.lang.ref.WeakReference;

import io.dcloud.PandoraEntryActivity;

public abstract class BaseMusicNotification {
    public static final int NOTIFICATION_ID = 0x111;
    public static final String CHANNEL_ID = "music_id_audio";
    public static final String CHANNEL_NAME = "music_name_audio";

    protected JSONObject songInfo;
    protected boolean isPlay;
    protected boolean showFavour;
    protected JSONObject mConfig;
    protected WeakReference<Context> mContext;
    protected Notification mNotification;
    protected MediaSessionCompat mMediaSession;
    protected NotificationManager mNotificationManager;

    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent getContentIntent(String path) {
        Intent intent = new Intent(mContext.get(), PandoraEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (path != null) {
            intent.putExtra(Global.KEY_PATH, path);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(mContext.get(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(mContext.get(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    /**
     * 创建Notification,
     *
     * @param service BaseMusicNotification.NotificationHelperListener
     * @param config  JSONObject
     */
    public void initNotification(Service service, JSONObject config) {
        mContext = new WeakReference<>(service);
        mConfig = config;

        mMediaSession = new MediaSessionCompat(service, BaseMusicNotification.CHANNEL_ID);
        mMediaSession.setActive(true);
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder().build());
        mMediaSession.setPlaybackState((new PlaybackStateCompat.Builder()).setActions(PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() == 0) {
                    JSONObject data = new JSONObject();
                    data.put("type", Global.MEDIA_BUTTON);
                    data.put("keyCode", keyEvent.getKeyCode());
                    ((PlayServiceV2) mContext.get()).fireGlobalEventCallback(Global.EVENT_MUSIC_MEDIA_BUTTON, data);
                }
                return true;
            }
        });
    }

    public abstract void createNotification();

    protected abstract void buildNotification();

    /**
     * 更新 Notification 信息
     *
     * @param options 歌曲信息
     */
    public void updateSong(JSONObject options) {
        songInfo = options;
        buildNotification();
    }

    /**
     * 切换播放状态
     *
     * @param isPlay 播放状态
     */
    public void playOrPause(boolean isPlay) {
        this.isPlay = isPlay;
        buildNotification();
    }

    /**
     * 切换搜藏状态
     *
     * @param favourite 搜藏状态
     */
    public void favour(boolean favourite) {
        songInfo.put(Global.KEY_FAVOUR, favourite);
        buildNotification();
    }

    public void cancel() {
        if (mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void setShowFavour(boolean show) {
        showFavour = show;
    }

    protected void generateGlide(Context context, Target<Bitmap> target, String picUrl, float width, float height) {
        Glide.with(context)
                .asBitmap()
                .load(picUrl)
                .sizeMultiplier(0.8f)
                .format(DecodeFormat.PREFER_RGB_565)
                .override(WXViewUtils.dip2px(width), WXViewUtils.dip2px(height))
                .into(target);
    }
}
