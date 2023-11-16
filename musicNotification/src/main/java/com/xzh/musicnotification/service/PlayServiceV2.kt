package com.xzh.musicnotification.service

import android.annotation.SuppressLint
import android.app.PendingIntent.CanceledException
import android.app.Service
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.Process
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.IMusicActivityCallbackAidlInterface
import com.xzh.musicnotification.IMusicServiceAidlInterface
import com.xzh.musicnotification.IMusicServiceCallbackAidlInterface
import com.xzh.musicnotification.LockActivityV2
import com.xzh.musicnotification.notification.BaseMusicNotification
import com.xzh.musicnotification.notification.MusicNotificationV2
import com.xzh.musicnotification.service.NotificationReceiver.IReceiverListener
import com.xzh.musicnotification.utils.Utils

class PlayServiceV2 : Service(), IReceiverListener {
    private var playing = false
    private var lockActivity = false
    var songData: MutableMap<String, Any?>? = null
    private var mReceiver: NotificationReceiver? = null
    private var mServiceEventListener: IMusicServiceCallbackAidlInterface? = null
    private var mActivityEventListener: IMusicActivityCallbackAidlInterface? = null

    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        MusicNotificationV2.instance.initNotification(
            this,
            object : BaseMusicNotification.OnMusicEventListener {
                override fun onMediaButtonEvent(keyCode: Int) {
                    val data = hashMapOf<String, Any>()
                    data["type"] = Global.MEDIA_BUTTON
                    data["keyCode"] = keyCode
                    mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_MEDIA_BUTTON, data)
                }

                override fun onPlay() {
                    val data = hashMapOf<String, Any>()
                    data["message"] = "更新成功"
                    data["code"] = 0
                    mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_NOTIFICATION_PAUSE, data)
                }

                override fun onPause() {
                    val data = hashMapOf<String, Any>()
                    data["message"] = "更新成功"
                    data["code"] = 0
                    mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_NOTIFICATION_PAUSE, data)
                }

                override fun onSkipToNext() {
                    val data = hashMapOf<String, Any>()
                    data["message"] = "更新成功"
                    data["code"] = 0
                    mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_NOTIFICATION_NEXT, data)
                }

                override fun onSkipToPrevious() {
                    val data = hashMapOf<String, Any>()
                    data["message"] = "更新成功"
                    data["code"] = 0
                    mServiceEventListener?.sendMessage(
                        Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS,
                        data
                    )
                }

                override fun onSeekTo(pos: Long) {
                    val data = hashMapOf<String, Any>()
                    data["position"] = pos / 1000
                    mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_SEEK_TO, data)
                }
            })

        MusicNotificationV2.instance.createNotification()
        mReceiver = NotificationReceiver(this)
        val filter = IntentFilter()
        // 锁屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        // 耳机广播
        filter.addAction(Intent.ACTION_HEADSET_PLUG)
        // 蓝牙广播
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        // 自定义广播
        filter.addAction(packageName + NotificationReceiver.ACTION_STATUS_BAR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(mReceiver, filter)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return object : IMusicServiceAidlInterface.Stub() {
            override fun initConfig(config: MutableMap<Any?, Any?>?) {
                MusicNotificationV2.instance.initConfig(config)
            }

            override fun setServiceEventListener(listener: IMusicServiceCallbackAidlInterface?) {
                mServiceEventListener = listener
                val data = hashMapOf<String, Any>()
                data["type"] = "destroy"
                listener?.sendMessage("setEventListener", data)
            }

            override fun setActivityEventListener(listener: IMusicActivityCallbackAidlInterface?) {
                mActivityEventListener = listener
            }

            override fun switchNotification(style: Boolean) {
                MusicNotificationV2.instance.switchNotification(style)
            }

            override fun setPosition(position: Long) {
                MusicNotificationV2.instance.setPosition(position)
            }

            override fun getFavour(): Boolean {
                return if (this@PlayServiceV2.songData != null) this@PlayServiceV2.songData!![Global.KEY_FAVOUR] as Boolean else false
            }

            override fun getPlaying(): Boolean {
                return this@PlayServiceV2.playing
            }

            override fun getSongData(): MutableMap<String, Any?>? {
                return this@PlayServiceV2.songData
            }

            override fun lock(locking: Boolean) {
                lockActivity = locking
            }

            override fun playOrPause(playing: Boolean) {
                this@PlayServiceV2.playing = playing
                mActivityEventListener?.playOrPause(playing)
                val options: MutableMap<String, Any?> = HashMap()
                options[Global.KEY_PLAYING] = playing
                invoke(this@PlayServiceV2, Global.KEY_PLAY_OR_PAUSE, options)
                MusicNotificationV2.instance.playOrPause(playing)
            }

            override fun setFavour(favour: Boolean) {
                if (songData != null) {
                    songData!![Global.KEY_FAVOUR] = favour
                    invoke(this@PlayServiceV2, Global.KEY_FAVOUR, this@PlayServiceV2.songData!!)
                }
                mActivityEventListener?.setFavour(favour)
                MusicNotificationV2.instance.favour(favour)
            }

            override fun update(option: MutableMap<Any?, Any?>?) {
                this@PlayServiceV2.songData = option as MutableMap<String, Any?>
                mActivityEventListener?.update(option)
                invoke(this@PlayServiceV2, Global.KEY_UPDATE, option)
                MusicNotificationV2.instance.updateSong(option)
            }

            override fun sendMessage(eventName: String?, params: MutableMap<Any?, Any?>?) {
                mServiceEventListener?.sendMessage(eventName, params)
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun onDestroy() {
        super.onDestroy()
        val data = hashMapOf<String, Any>()
        data["type"] = "destroy"
        mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_LIFECYCLE, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(MusicNotificationV2.instance.iD)
        } else {
            stopForeground(true)
        }
        MusicNotificationV2.instance.cancel()
        unregisterReceiver(mReceiver)
        if (!Utils.isMainProcess(this)) {
            Process.killProcess(Process.myPid())
        }
    }

    override fun onScreenReceive() {
        try {
            if (lockActivity) Utils.openLock(this@PlayServiceV2, LockActivityV2::class.java)
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }

    override fun onHeadsetReceive(extra: Int) {
        val data = hashMapOf<String, Any>()
        data["type"] = Global.MEDIA_BUTTON_HEADSET
        data["keyCode"] = extra
        mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_MEDIA_BUTTON, data)
    }

    override fun onBluetoothReceive(extra: Int) {
        val data = hashMapOf<String, Any>()
        data["type"] = Global.MEDIA_BUTTON_BLUETOOTH
        data["keyCode"] = extra
        mServiceEventListener?.sendMessage(Global.EVENT_MUSIC_MEDIA_BUTTON, data)
    }

    override fun onMusicReceive(extra: String?) {
        var eventName = Global.EVENT_MUSIC_NOTIFICATION_ERROR
        val data = hashMapOf<String, Any>()
        data["message"] = "触发回调事件成功"
        data["code"] = 0
        when (extra) {
            NotificationReceiver.EXTRA_PLAY -> eventName = Global.EVENT_MUSIC_NOTIFICATION_PAUSE
            NotificationReceiver.EXTRA_PRE -> eventName = Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS
            NotificationReceiver.EXTRA_NEXT -> eventName = Global.EVENT_MUSIC_NOTIFICATION_NEXT
            NotificationReceiver.EXTRA_FAV -> eventName = Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE
            NotificationReceiver.EXTRA_CLOSE -> eventName = Global.EVENT_MUSIC_NOTIFICATION_CLOSE
            "enabled" -> {
                if (songData != null) this@PlayServiceV2.let {
                    invoke(it, Global.KEY_UPDATE, songData!!)
                    val options: MutableMap<String, Any?> = HashMap()
                    options[Global.KEY_PLAYING] = playing
                    invoke(it, Global.KEY_PLAY_OR_PAUSE, options)
                }
            }

            else -> {
                data["message"] = "触发回调事件失败"
                data["code"] = -7
            }
        }
        mServiceEventListener?.sendMessage(eventName, data)
    }

    fun invoke(context: Context, type: String, options: MutableMap<String, Any?>) {
        try {
            val clazz = Class.forName("com.xzh.widget.MusicWidget")

            val method = clazz.getDeclaredMethod(
                "invoke",
                Context::class.java,
                String::class.java,
                MutableMap::class.java
            )
            method.invoke(clazz, context, type, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}