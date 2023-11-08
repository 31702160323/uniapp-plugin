package com.xzh.musicnotification.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.Target
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.service.PlayServiceV2
import com.xzh.musicnotification.utils.Utils
import io.dcloud.PandoraEntryActivity
import java.lang.ref.WeakReference


abstract class BaseMusicNotification {
    var iD = 0x111
        protected set

    @JvmField
    protected var position = 0L

    @JvmField
    protected var isPlay = false

    @JvmField
    protected var songInfo: JSONObject? = null

    @JvmField
    protected var mContext: WeakReference<Context>? = null

    @JvmField
    protected var mNotification: Notification? = null

    @JvmField
    protected var mMediaSession: MediaSessionCompat? = null

    @JvmField
    protected var mNotificationManager: NotificationManager? = null

    @JvmField
    protected var mPlaybackStateBuilder: PlaybackStateCompat.Builder? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    protected fun getContentIntent(path: String?): PendingIntent {
        val intent = Intent(mContext!!.get(), PandoraEntryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (path != null) {
            intent.putExtra(Global.KEY_PATH, path)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(mContext!!.get(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(
                mContext!!.get(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    /**
     * 创建Notification,
     *
     * @param service BaseMusicNotification.NotificationHelperListener
     */
    fun initNotification(service: Service) {
        mContext = WeakReference(service)
        iD += 1
        mMediaSession = MediaSessionCompat(service, CHANNEL_ID)
        mMediaSession!!.isActive = true
//        mMediaSession!!.setMetadata(MediaMetadataCompat.Builder().build())

        mPlaybackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
//                            PlaybackStateCompat.ACTION_SET_RATING or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
//                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
//                            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
//                            PlaybackStateCompat.ACTION_PLAY_FROM_URI or
//                            PlaybackStateCompat.ACTION_PREPARE or
//                            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
//                            PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
//                            PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
                        PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                        PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
//                            PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED or
//                            PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )

        // 使用新的播放状态更新MediaSessionCompat实例
        mMediaSession!!.setPlaybackState(mPlaybackStateBuilder?.build())


        mMediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(intent: Intent): Boolean {
                val keyEvent: KeyEvent? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }
                if (keyEvent?.action == 0) {
                    val data = JSONObject()
                    data["type"] = Global.MEDIA_BUTTON
                    data["keyCode"] = keyEvent.keyCode
                    (mContext!!.get() as PlayServiceV2?)!!.fireGlobalEventCallback(
                        Global.EVENT_MUSIC_MEDIA_BUTTON,
                        data
                    )
                }
                return true
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                this@BaseMusicNotification.setPosition(pos)

                // 设置播放状态为正在播放，并设置媒体播放的当前位置
                mMediaSession!!.setPlaybackState(
                    mPlaybackStateBuilder
                        ?.setState(
                            if (this@BaseMusicNotification.isPlay) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_STOPPED,
                            this@BaseMusicNotification.position, 1.0F
                        )
                        ?.build()
                )

                val data = JSONObject()
                data["position"] = pos / 1000
                (mContext!!.get() as PlayServiceV2?)!!.fireGlobalEventCallback(
                    Global.EVENT_MUSIC_SEEK_TO,
                    data
                )
            }
        })
    }

    abstract fun createNotification()
    protected abstract fun updateNotification()

    /**
     * 更新 Notification 信息
     *
     * @param options 歌曲信息
     */
    fun updateSong(options: JSONObject?) {
        songInfo = options
        updateNotification()
    }

    /**
     * 切换播放状态
     *
     * @param isPlay 播放状态
     */
    fun playOrPause(isPlay: Boolean) {
        this.isPlay = isPlay
        updateNotification()
    }

    @JvmName("setBasePosition")
    fun setPosition(position: Long) {
        this.position = position
    }

    /**
     * 切换搜藏状态
     *
     * @param favourite 搜藏状态
     */
    fun favour(favourite: Boolean) {
        if (songInfo != null) songInfo!![Global.KEY_FAVOUR] = favourite
        updateNotification()
    }

    fun cancel() {
        if (mNotificationManager != null) {
            mNotification = null
            mNotificationManager!!.cancel(iD)
        }
    }

    protected fun generateGlide(
        context: Context?,
        target: Target<Bitmap>,
        picUrl: String?,
        width: Float,
        height: Float
    ) {
        Glide.with(context!!)
            .asBitmap()
            .load(picUrl)
            .sizeMultiplier(0.5f)
            .format(DecodeFormat.PREFER_RGB_565)
            .override(Utils.dip2px(width), Utils.dip2px(height))
            .into(target)
    }

    companion object {
        internal const val CHANNEL_ID = "music_id_audio"

        internal const val CHANNEL_NAME = "music_name_audio"
        internal var mConfig: JSONObject? = null

        internal var showFavour = false
    }
}