package com.xzh.musicnotification.notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.R
import com.xzh.musicnotification.service.NotificationReceiver
import com.xzh.musicnotification.utils.PendingIntentInfo
import com.xzh.musicnotification.utils.Utils
import io.dcloud.PandoraEntryActivity
import java.lang.ref.WeakReference


@Suppress("DEPRECATION")
open class MusicNotificationV2 {

    companion object {
        internal const val CHANNEL_ID = "music_id_audio"

        internal const val CHANNEL_NAME = "music_name_audio"
        val instance: MusicNotificationV2
            get() = SingletonHolder.instance
    }

    var iD = 0x111
        protected set

    private var position = 0L

    private var isPlay = false

    private var showFavour = false

    private var systemStyle = false

    // 大布局
    private var mRemoteViews: RemoteViews? = null

    //小布局
    private var mSmallRemoteViews: RemoteViews? = null

    private var mConfig: MutableMap<Any?, Any?>? = null

    private var songInfo: MutableMap<Any?, Any?>? = null

    private var mContext: WeakReference<Context>? = null

    private var mNotification: Notification? = null

    private var mMediaSession: MediaSessionCompat? = null

    private var mNotificationManager: NotificationManager? = null

    private var mMediaSessionCallback: MediaSessionCompat.Callback? = null

    private var mPlaybackStateBuilder: PlaybackStateCompat.Builder? = null

    //点击播放按钮要发送的广播
    //点击上一首按钮要发送的广播
    //点击下一首按钮要发送的广播
    //点击收藏按钮要发送的广播
    private val remoteViews: RemoteViews?
        get() {
            if (mRemoteViews == null) {
                val packageName = mContext?.get()?.packageName
                mRemoteViews = RemoteViews(packageName, R.layout.notification_big_layout)
                mRemoteViews?.setTextViewText(R.id.title_view, "开启美好的一天")
                mRemoteViews?.setImageViewResource(R.id.next_view, R.drawable.note_btn_next_white)
                mRemoteViews?.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white)
                mRemoteViews?.setImageViewResource(
                    R.id.previous_view,
                    R.drawable.note_btn_pre_white
                )
                if (showFavour) {
                    mRemoteViews?.setViewVisibility(R.id.favourite_view, View.VISIBLE)
                    mRemoteViews?.setImageViewResource(
                        R.id.favourite_view,
                        R.drawable.note_btn_love_white
                    )
                }
                PendingIntentInfo.addOnClickPendingIntents(
                    mRemoteViews, mContext?.get(),  //点击播放按钮要发送的广播
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
                    PendingIntentInfo(R.id.favourite_view, 4, NotificationReceiver.EXTRA_FAV),
                    //点击关闭按钮要发送的广播
                    PendingIntentInfo(R.id.note_close, 5, NotificationReceiver.EXTRA_CLOSE)
                )
            }
            return mRemoteViews
        }

    //点击播放按钮要发送的广播
    //点击上一首按钮要发送的广播
    //点击下一首按钮要发送的广播
    private val smallRemoteViews: RemoteViews?
        get() {
            if (mSmallRemoteViews == null) {
                val packageName = mContext?.get()?.packageName
                mSmallRemoteViews = RemoteViews(packageName, R.layout.notification_small_layout)
                mSmallRemoteViews?.setTextViewText(R.id.title_view, "开启美好的一天")
                mSmallRemoteViews?.setImageViewResource(
                    R.id.previous_view,
                    R.drawable.note_btn_pre_white
                )
                mSmallRemoteViews?.setImageViewResource(
                    R.id.next_view,
                    R.drawable.note_btn_next_white
                )
                PendingIntentInfo.addOnClickPendingIntents(
                    mSmallRemoteViews, mContext?.get(),  //点击播放按钮要发送的广播
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
                    PendingIntentInfo(R.id.next_view, 3, NotificationReceiver.EXTRA_NEXT),
                    // 点击关闭按钮要发送的广播
                    PendingIntentInfo(R.id.note_close, 5, NotificationReceiver.EXTRA_CLOSE)
                )
            }
            return mSmallRemoteViews
        }

    private object SingletonHolder {
        val instance = MusicNotificationV2()
    }

    fun initConfig(config: MutableMap<Any?, Any?>?) {
        mConfig = config
        createNotification()
    }

    fun initNotification(service: Service, listener: OnMusicEventListener) {
        mContext = WeakReference(service)

        val info = Utils.getApplicationInfo(service)
        if (info != null) {
            showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR)
        }

        mPlaybackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
//            PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
//                        PlaybackStateCompat.ACTION_SET_RATING or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
//                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
//                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
//                        PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
//                        PlaybackStateCompat.ACTION_PLAY_FROM_URI or
//                        PlaybackStateCompat.ACTION_PREPARE or
//                        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
//                        PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
//                        PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
//                        PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
//                    PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
//                        PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED or
//                        PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )

        mMediaSessionCallback = object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(intent: Intent): Boolean {
                val keyEvent: KeyEvent? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }
                if (keyEvent?.action == 0) {
                    listener.onMediaButtonEvent(keyEvent.keyCode)
                }
                return true
            }

            override fun onPlay() {
                listener.onPlay()
            }

            override fun onPause() {
                listener.onPause()
            }

            override fun onSkipToNext() {
                listener.onSkipToNext()
            }

            override fun onSkipToPrevious() {
                listener.onSkipToPrevious()
            }

            override fun onSeekTo(pos: Long) {
                this@MusicNotificationV2.setPosition(pos)
                // 设置播放状态为正在播放，并设置媒体播放的当前位置
                mMediaSession?.setPlaybackState(
                    mPlaybackStateBuilder
                        ?.setState(
                            if (this@MusicNotificationV2.isPlay) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_STOPPED,
                            pos, 1.0F
                        )
                        ?.build()
                )
                listener.onSeekTo(pos)
            }
        }
    }

    private fun initMediaSession(context: Context) {
        mMediaSession = MediaSessionCompat(context, CHANNEL_ID)
        mMediaSession?.isActive = true
//        mMediaSession!!.setMetadata(MediaMetadataCompat.Builder().build())

        // 使用新的播放状态更新MediaSessionCompat实例
        mMediaSession?.setPlaybackState(mPlaybackStateBuilder?.build())

        if (Utils.isUiThread()) {
            mMediaSession?.setCallback(mMediaSessionCallback)
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                mMediaSession?.setCallback(mMediaSessionCallback)
            }
        }
    }

    fun createNotification() {
        val context = mContext!!.get()
        if (context === null) return
        if (mMediaSession === null) initMediaSession(context)
        if (mNotificationManager === null) mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val path = mConfig?.get(Global.KEY_PATH).toString()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) { //Android 5.1 以下
            mNotification = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.music_icon)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(getContentIntent(path))
                .build()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //Android 8.0以下
            mNotification = NotificationCompat.Builder(context, CHANNEL_ID)
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
            mNotificationManager?.createNotificationChannel(notificationChannel)
            if (!systemStyle) {
                val builder = NotificationCompat.Builder(
                    context, CHANNEL_ID
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
        if (songInfo != null) {
            updateSong(songInfo!!)
        }
        playOrPause(isPlay)

        // 设置为前台Service
        if (mNotification != null) {
            (context as Service?)!!.startForeground(
                iD,
                mNotification
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateNotification() {
        val context = mContext!!.get()
        if (context === null) return
        if (this@MusicNotificationV2.songInfo == null) return
        if (this@MusicNotificationV2.mNotificationManager == null) createNotification()
        Utils.debounce({
            this@MusicNotificationV2.songInfo?.run {
                val picUrl = this["picUrl"].toString()
                val songName = this[Global.KEY_SONG_NAME].toString()
                val artistsName = this[Global.KEY_ARTISTS_NAME].toString()
                val duration =
                    if (this[Global.KEY_DURATION] == null) 0L else this[Global.KEY_DURATION] as Int
                val play =
                    if (this@MusicNotificationV2.isPlay) R.drawable.note_btn_pause_white else R.drawable.note_btn_play_white
                val favour =
                    if (this[Global.KEY_FAVOUR] != null && this[Global.KEY_FAVOUR] as Boolean
                    ) R.drawable.note_btn_loved else R.drawable.note_btn_love_white
                if (this@MusicNotificationV2.systemStyle) {
                    val dip64 = Utils.dip2px(64f)
                    generateGlide(
                        context,
                        object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                mMediaSession?.setMetadata(
                                    MediaMetadataCompat.Builder()
                                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                                        .putString(
                                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                                            artistsName
                                        )
                                        .putLong(
                                            MediaMetadataCompat.METADATA_KEY_DURATION,
                                            duration.toLong()
                                        )
                                        .build()
                                )

                                // 设置播放状态为正在播放，并设置媒体播放的当前位置
                                mMediaSession?.setPlaybackState(
                                    mPlaybackStateBuilder?.setState(
                                        if (this@MusicNotificationV2.isPlay) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_STOPPED,
                                        this@MusicNotificationV2.position, 1.0F
                                    )
                                        ?.build()
                                )

                                val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                                mediaStyle.setMediaSession(this@MusicNotificationV2.mMediaSession?.sessionToken)
                                mediaStyle.setShowCancelButton(true)

                                val builder = NotificationCompat.Builder(
                                    context, CHANNEL_ID
                                ) //                                .setColorized(true)
                                    .setOngoing(false)
                                    .setShowWhen(false)
                                    .setAutoCancel(false)
                                    .setOnlyAlertOnce(true)
                                    .setSmallIcon(R.drawable.music_icon) //                                .setBadgeIconType(R.drawable.music_icon)
//                                    .setLargeIcon()
                                    .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setPriority(NotificationCompat.PRIORITY_LOW)
                                    .setContentIntent(
                                        getContentIntent(
                                            mConfig?.get(Global.KEY_PATH).toString()
                                        )
                                    )
                                    .setContentTitle(songName)
                                    .setContentText(artistsName)
                                    .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                                    .setLargeIcon(resource)
//                                    .setProgress(0, 0, true)
                                    .setStyle(mediaStyle)

                                if (showFavour) {
                                    mediaStyle.setShowActionsInCompactView(1, 2, 3)
                                    builder.addAction(
                                        generateAction(
                                            favour,
                                            "Favourite",
                                            3,
                                            NotificationReceiver.EXTRA_FAV
                                        )
                                    )
                                } else {
                                    mediaStyle.setShowActionsInCompactView(0, 1, 2)
                                }

                                val notification = builder.addAction(
                                    generateAction(
                                        R.drawable.note_btn_pre_white,
                                        "Previous",
                                        1,
                                        NotificationReceiver.EXTRA_PRE
                                    )
                                ).addAction(
                                    generateAction(
                                        play,
                                        "Play",
                                        0,
                                        NotificationReceiver.EXTRA_PLAY
                                    )
                                ).addAction(
                                    generateAction(
                                        R.drawable.note_btn_next_white,
                                        "Next",
                                        2,
                                        NotificationReceiver.EXTRA_NEXT
                                    )
                                ).build()

                                if (this@MusicNotificationV2.mNotification == null) {
                                    (context as Service).startForeground(
                                        iD,
                                        notification
                                    )
                                } else {
                                    this@MusicNotificationV2.mNotificationManager?.notify(
                                        iD,
                                        notification
                                    )
                                }
                                this@MusicNotificationV2.mNotification = notification
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onLoadFailed(errorDrawable: Drawable?) {}
                        } as Target<Bitmap>,
                        picUrl,
                        dip64.toFloat(),
                        dip64.toFloat())
                } else {
                    if (showFavour) {
                        this@MusicNotificationV2.mRemoteViews?.setImageViewResource(
                            R.id.favourite_view,
                            favour
                        )
                    }
                    this@MusicNotificationV2.mRemoteViews?.setTextViewText(
                        R.id.title_view,
                        songName
                    )
                    this@MusicNotificationV2.mRemoteViews?.setTextViewText(
                        R.id.tip_view,
                        artistsName
                    )
                    this@MusicNotificationV2.mRemoteViews?.setImageViewResource(
                        R.id.play_view,
                        play
                    )
                    setPicUrlBitmap(
                        context,
                        this@MusicNotificationV2.mRemoteViews,
                        picUrl,
                        112f,
                        112f
                    )
                    if (this@MusicNotificationV2.mSmallRemoteViews != null) {
                        this@MusicNotificationV2.mSmallRemoteViews?.setTextViewText(
                            R.id.tip_view,
                            artistsName
                        )
                        this@MusicNotificationV2.mSmallRemoteViews?.setTextViewText(
                            R.id.title_view,
                            songName
                        )
                        this@MusicNotificationV2.mSmallRemoteViews?.setImageViewResource(
                            R.id.play_view,
                            play
                        )
                        setPicUrlBitmap(
                            context,
                            this@MusicNotificationV2.mSmallRemoteViews,
                            picUrl,
                            64f,
                            64f
                        )
                    }
                    this@MusicNotificationV2.mNotificationManager?.notify(
                        this@MusicNotificationV2.iD,
                        this@MusicNotificationV2.mNotification
                    )
                }
            }
        }, 500)
    }

    /**
     * 更新 Notification 信息
     *
     * @param options 歌曲信息
     */
    fun updateSong(options: MutableMap<Any?, Any?>) {
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

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun generateAction(
        icon: Int,
        title: CharSequence,
        requestCode: Int,
        EXTRA: String
    ): NotificationCompat.Action {
        val pendingIntent: PendingIntent
        val context = mContext!!.get()!!
        val intent = Intent(context.packageName + NotificationReceiver.ACTION_STATUS_BAR)
        //        Intent intent = new Intent(mContext.get(), NotificationReceiver.class);
//        intent.setAction(mContext.get().getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
        intent.setPackage(context.packageName)
        intent.putExtra(NotificationReceiver.EXTRA, EXTRA)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    fun switchNotification(style: Boolean) {
        systemStyle = style
        cancel()
        createNotification()
    }

    fun cancel() {
        mNotificationManager?.cancel(iD)
        mNotification = null
        mMediaSession?.release()
        mMediaSession = null
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

    private fun generateGlide(
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

    interface OnMusicEventListener {
        fun onMediaButtonEvent(keyCode: Int)
        fun onPlay()
        fun onPause()
        fun onSkipToNext()
        fun onSkipToPrevious()
        fun onSeekTo(pos: Long)
    }
}