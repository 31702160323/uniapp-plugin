package com.xzh.musicnotification.notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.R
import com.xzh.musicnotification.service.NotificationReceiver
import com.xzh.musicnotification.utils.PendingIntentInfo
import com.xzh.musicnotification.utils.Utils


@Suppress("DEPRECATION")
open class MusicNotificationV2 : BaseMusicNotification() {
    private var systemStyle = false

    private var position = 0L

    // 大布局
    private var mRemoteViews: RemoteViews? = null

    //小布局
    private var mSmallRemoteViews: RemoteViews? = null

    private object SingletonHolder {
        val instance = MusicNotificationV2()
    }

    override fun createNotification() {
        cancel()
        if (mConfig == null) return
        mNotificationManager =
            mContext!!.get()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val path = mConfig!!.getString(Global.KEY_PATH).toString()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) { //Android 5.1 以下
            mNotification = Notification.Builder(mContext!!.get())
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.music_icon)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(getContentIntent(path))
                .build()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //Android 8.0以下
            mNotification = NotificationCompat.Builder(mContext!!.get()!!, CHANNEL_ID)
                .setOngoing(true)
                .setColorized(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setContent(smallRemoteViews)
                .setSmallIcon(R.drawable.music_icon)
                .setPriority(Notification.PRIORITY_LOW)
                .setCustomBigContentView(remoteViews) //展开视图
                .setCustomContentView(smallRemoteViews)
                .setContentIntent(getContentIntent(path))
                .build()
        } else {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            mNotificationManager!!.createNotificationChannel(notificationChannel)
            if (!systemStyle) {
                val builder = NotificationCompat.Builder(
                    mContext!!.get()!!, CHANNEL_ID
                ) //                        .setOngoing(true)
                    //                        .setColorized(true)
                    //                        .setShowWhen(false)
                    //                        .setOnlyAlertOnce(true)
                    .setOngoing(false)
                    .setShowWhen(true)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.music_icon) //                        .setBadgeIconType(R.drawable.music_icon)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(getContentIntent(path))
                    .setCustomBigContentView(remoteViews) //展开视图
                    .setCustomContentView(smallRemoteViews)
                mNotification = builder.build()
            }
        }
        updateSong(songInfo)
        playOrPause(isPlay)

        // 设置为前台Service
        if (mNotification != null) (mContext!!.get() as Service?)!!.startForeground(
            iD,
            mNotification
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun updateNotification() {
        if (mConfig == null) return
        if (this@MusicNotificationV2.songInfo == null) return
        if (this@MusicNotificationV2.mNotificationManager == null) createNotification()
        Utils.debounce({
            val picUrl = this@MusicNotificationV2.songInfo!!.getString("picUrl").toString()
            val songName = this@MusicNotificationV2.songInfo!!.getString(Global.KEY_SONG_NAME).toString()
            val artistsName = this@MusicNotificationV2.songInfo!!.getString(Global.KEY_ARTISTS_NAME).toString()
            val duration = this@MusicNotificationV2.songInfo!!.getLong(Global.KEY_DURATION).toLong()
            val play =
                if (this@MusicNotificationV2.isPlay) R.drawable.note_btn_pause_white else R.drawable.note_btn_play_white
            val favour =
                if (this@MusicNotificationV2.songInfo!!.getBoolean(Global.KEY_FAVOUR) != null && songInfo!!.getBoolean(
                        Global.KEY_FAVOUR
                    )
                ) R.drawable.note_btn_loved else R.drawable.note_btn_love_white
            if (this@MusicNotificationV2.systemStyle) {
                val dip64 = Utils.dip2px(64f)
                generateGlide(this@MusicNotificationV2.mContext!!.get(), object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        mMediaSession!!.setMetadata(
                            MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistsName)
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                            .build())

                        // 设置播放状态为正在播放，并设置媒体播放的当前位置
                        mMediaSession!!.setPlaybackState(PlaybackStateCompat.Builder()
                            .setState(if(this@MusicNotificationV2.isPlay) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_STOPPED,
                                this@MusicNotificationV2.position, 1.0f)
                            .build())

                        val builder = NotificationCompat.Builder(
                            this@MusicNotificationV2.mContext!!.get()!!, CHANNEL_ID
                        ) //                                .setColorized(true)
                            .setOngoing(false)
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.drawable.music_icon) //                                .setBadgeIconType(R.drawable.music_icon)
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentIntent(
                                getContentIntent(
                                    mConfig!!.getString(Global.KEY_PATH).toString()
                                )
                            )
                            .setContentTitle(songName)
                            .setContentText(artistsName)
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                            .setLargeIcon(resource)
//                            .setProgress(60 * 4 * 1000, 60 * 500, false)
                        val style = androidx.media.app.NotificationCompat.MediaStyle()
                        style.setMediaSession(this@MusicNotificationV2.mMediaSession!!.sessionToken)
                        style.setShowCancelButton(true)
                        if (showFavour) {
                            style.setShowActionsInCompactView(1, 2, 3)
                            builder.addAction(
                                generateAction(
                                    favour,
                                    "Favourite",
                                    3,
                                    NotificationReceiver.EXTRA_FAV
                                )
                            )
                        } else {
                            style.setShowActionsInCompactView(0, 1, 2)
                        }
                        builder.addAction(
                            generateAction(
                                R.drawable.note_btn_pre_white,
                                "Previous",
                                1,
                                NotificationReceiver.EXTRA_PRE
                            )
                        )
                        builder.addAction(
                            generateAction(
                                play,
                                "Play",
                                0,
                                NotificationReceiver.EXTRA_PLAY
                            )
                        )
                        builder.addAction(
                            generateAction(
                                R.drawable.note_btn_next_white,
                                "Next",
                                2,
                                NotificationReceiver.EXTRA_NEXT
                            )
                        )
                        builder.setStyle(style)
                        val notification = builder.build()
                        if (this@MusicNotificationV2.mNotification == null) {
                            (mContext!!.get() as Service?)!!.startForeground(iD, notification)
                        } else {
                            this@MusicNotificationV2.mNotificationManager!!.notify(iD, notification)
                        }
                        this@MusicNotificationV2.mNotification = notification
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onLoadFailed(errorDrawable: Drawable?) {}
                } as Target<Bitmap>, picUrl, dip64.toFloat(), dip64.toFloat())
            } else {
                if (showFavour) {
                    this@MusicNotificationV2.mRemoteViews!!.setImageViewResource(R.id.favourite_view, favour)
                }
                this@MusicNotificationV2.mRemoteViews!!.setTextViewText(R.id.title_view, songName)
                this@MusicNotificationV2.mRemoteViews!!.setTextViewText(R.id.tip_view, artistsName)
                this@MusicNotificationV2.mRemoteViews!!.setImageViewResource(R.id.play_view, play)
                setPicUrlBitmap(mContext!!.get(), this@MusicNotificationV2.mRemoteViews, picUrl, 112f, 112f)
                if (this@MusicNotificationV2.mSmallRemoteViews != null) {
                    this@MusicNotificationV2.mSmallRemoteViews!!.setTextViewText(R.id.tip_view, artistsName)
                    this@MusicNotificationV2.mSmallRemoteViews!!.setTextViewText(R.id.title_view, songName)
                    this@MusicNotificationV2.mSmallRemoteViews!!.setImageViewResource(R.id.play_view, play)
                    setPicUrlBitmap(mContext!!.get(), this@MusicNotificationV2.mSmallRemoteViews, picUrl, 64f, 64f)
                }
                this@MusicNotificationV2.mNotificationManager!!.notify(this@MusicNotificationV2.iD, this@MusicNotificationV2.mNotification)
            }
        }, 500)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun generateAction(
        icon: Int,
        title: CharSequence,
        requestCode: Int,
        EXTRA: String
    ): NotificationCompat.Action {
        val pendingIntent: PendingIntent
        val intent = Intent(mContext!!.get()!!.packageName + NotificationReceiver.ACTION_STATUS_BAR)
        //        Intent intent = new Intent(mContext.get(), NotificationReceiver.class);
//        intent.setAction(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
        intent.setPackage(mContext!!.get()!!.packageName)
        intent.putExtra(NotificationReceiver.EXTRA, EXTRA)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                mContext!!.get(),
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                mContext!!.get(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    //点击播放按钮要发送的广播
    //点击上一首按钮要发送的广播
    //点击下一首按钮要发送的广播
    //点击收藏按钮要发送的广播
    private val remoteViews: RemoteViews
        get() {
            if (mRemoteViews == null) {
                val packageName = mContext!!.get()!!.packageName
                mRemoteViews = RemoteViews(packageName, R.layout.notification_big_layout)
                mRemoteViews?.setTextViewText(R.id.title_view, "开启美好的一天")
                mRemoteViews!!.setImageViewResource(R.id.next_view, R.drawable.note_btn_next_white)
                mRemoteViews!!.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white)
                mRemoteViews!!.setImageViewResource(
                    R.id.previous_view,
                    R.drawable.note_btn_pre_white
                )
                if (showFavour) {
                    mRemoteViews!!.setViewVisibility(R.id.favourite_view, View.VISIBLE)
                    mRemoteViews!!.setImageViewResource(
                        R.id.favourite_view,
                        R.drawable.note_btn_love_white
                    )
                }
                PendingIntentInfo.addOnClickPendingIntents(
                    mRemoteViews!!, mContext!!.get()!!,  //点击播放按钮要发送的广播
                    PendingIntentInfo(
                        R.id.play_view,
                        1,
                        NotificationReceiver.EXTRA_PLAY
                    ),  //点击上一首按钮要发送的广播
                    PendingIntentInfo(
                        R.id.previous_view,
                        2,
                        NotificationReceiver.EXTRA_PRE
                    ),  //点击下一首按钮要发送的广播
                    PendingIntentInfo(
                        R.id.next_view,
                        3,
                        NotificationReceiver.EXTRA_NEXT
                    ),  //点击收藏按钮要发送的广播
                    PendingIntentInfo(R.id.favourite_view, 4, NotificationReceiver.EXTRA_FAV)
                )
            }
            return mRemoteViews!!
        }

    //点击播放按钮要发送的广播
    //点击上一首按钮要发送的广播
    //点击下一首按钮要发送的广播
    private val smallRemoteViews: RemoteViews
        get() {
            if (mSmallRemoteViews == null) {
                val packageName = mContext!!.get()!!.packageName
                mSmallRemoteViews = RemoteViews(packageName, R.layout.notification_small_layout)
                mSmallRemoteViews!!.setTextViewText(R.id.title_view, "开启美好的一天")
                mSmallRemoteViews!!.setImageViewResource(
                    R.id.previous_view,
                    R.drawable.note_btn_pre_white
                )
                mSmallRemoteViews!!.setImageViewResource(
                    R.id.next_view,
                    R.drawable.note_btn_next_white
                )
                PendingIntentInfo.addOnClickPendingIntents(
                    mSmallRemoteViews!!, mContext!!.get()!!,  //点击播放按钮要发送的广播
                    PendingIntentInfo(
                        R.id.play_view,
                        1,
                        NotificationReceiver.EXTRA_PLAY
                    ),  //点击上一首按钮要发送的广播
                    PendingIntentInfo(
                        R.id.previous_view,
                        2,
                        NotificationReceiver.EXTRA_PRE
                    ),  //点击下一首按钮要发送的广播
                    PendingIntentInfo(R.id.next_view, 3, NotificationReceiver.EXTRA_NEXT)
                )
            }
            return mSmallRemoteViews!!
        }

    private fun setPicUrlBitmap(
        context: Context?,
        remoteViews: RemoteViews?,
        picUrl: String,
        width: Float,
        height: Float
    ) {
        val target = NotificationTarget(
            context,
            R.id.image_view,
            remoteViews,
            mNotification,
            iD
        )
        generateGlide(context, target, picUrl, width, height)
    }

    fun switchNotification(style: Boolean) {
        systemStyle = style
        if (this.mNotificationManager != null) {
            this.createNotification()
        }
    }

    fun setPosition(position: Long) {
        this.position = position
    }

    companion object {
        val instance: MusicNotificationV2
            get() = SingletonHolder.instance

        @JvmStatic
        fun initConfig(config: JSONObject?) {
            mConfig = config
        }

        @JvmStatic
        fun setShowFavour(show: Boolean) {
            showFavour = show
        }
    }
}