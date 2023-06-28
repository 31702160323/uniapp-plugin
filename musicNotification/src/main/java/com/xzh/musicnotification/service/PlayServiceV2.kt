package com.xzh.musicnotification.service

import android.annotation.SuppressLint
import android.app.PendingIntent.CanceledException
import android.app.Service
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.ArrayMap
import com.alibaba.fastjson.JSONObject
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.LockActivityV2
import com.xzh.musicnotification.notification.MusicNotificationV2
import com.xzh.musicnotification.service.NotificationReceiver.IReceiverListener
import com.xzh.musicnotification.utils.Utils
import java.lang.ref.WeakReference

class PlayServiceV2 : Service(), IReceiverListener {
    private var playing = false
    private var lockActivity = false
    var songData: JSONObject? = null
    private var mBinder: ServiceBinder? = null
    private var mReceiver: NotificationReceiver? = null
    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        service = this
        MusicNotificationV2.instance.initNotification(this)
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

    override fun onBind(intent: Intent): IBinder? {
        mBinder = ServiceBinder()
        return mBinder
    }

    @SuppressLint("WrongConstant")
    override fun onDestroy() {
        super.onDestroy()
        val data = JSONObject()
        data["type"] = "destroy"
        fireGlobalEventCallback(Global.EVENT_MUSIC_LIFECYCLE, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(MusicNotificationV2.instance.iD)
        } else {
            stopForeground(true)
        }
        MusicNotificationV2.instance.cancel()
        unregisterReceiver(mReceiver)
        service = null
    }

    override fun onScreenReceive() {
        try {
            if (lockActivity) Utils.openLock(this@PlayServiceV2, LockActivityV2::class.java)
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }

    override fun onHeadsetReceive(extra: Int) {
        val data = JSONObject()
        data["type"] = Global.MEDIA_BUTTON_HEADSET
        data["keyCode"] = extra
        fireGlobalEventCallback(Global.EVENT_MUSIC_MEDIA_BUTTON, data)
    }

    override fun onBluetoothReceive(extra: Int) {
        val data = JSONObject()
        data["type"] = Global.MEDIA_BUTTON_BLUETOOTH
        data["keyCode"] = extra
        fireGlobalEventCallback(Global.EVENT_MUSIC_MEDIA_BUTTON, data)
    }

    override fun onMusicReceive(extra: String?) {
        var eventName = Global.EVENT_MUSIC_NOTIFICATION_ERROR
        val data = JSONObject()
        data["message"] = "触发回调事件成功"
        data["code"] = 0
        when (extra) {
            NotificationReceiver.EXTRA_PLAY -> eventName = Global.EVENT_MUSIC_NOTIFICATION_PAUSE
            NotificationReceiver.EXTRA_PRE -> eventName = Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS
            NotificationReceiver.EXTRA_NEXT -> eventName = Global.EVENT_MUSIC_NOTIFICATION_NEXT
            NotificationReceiver.EXTRA_FAV -> eventName = Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE
            "enabled" -> {
                if (songData != null) service?.let {
                    invoke(it, Global.KEY_UPDATE, songData!!)
                    val options: MutableMap<String, Any> = ArrayMap()
                    options[Global.KEY_PLAYING] = playing
                    invoke(it, Global.KEY_PLAY_OR_PAUSE, options)
                }
            }
            else -> {
                data["message"] = "触发回调事件失败"
                data["code"] = -7
            }
        }
        if (mBinder != null) {
            mBinder!!.sendMessage(eventName, data)
        }
    }

    fun fireGlobalEventCallback(eventName: String, params: Map<String, Any>) {
        if (mBinder != null) {
            mBinder!!.sendMessage(eventName, params)
        }
    }

    inner class ServiceBinder : Binder() {
        private var mClickListener: WeakReference<OnClickListener?>? = null
        private var mEventListener: WeakReference<OnEventListener?>? = null
        fun setEventListener(eventListener: OnEventListener?) {
            mEventListener = WeakReference(eventListener)
        }

        fun setClickListener(clickListener: OnClickListener?) {
            mClickListener = WeakReference(clickListener)
        }

        fun switchNotification(style: Boolean) {
            MusicNotificationV2.instance.switchNotification(style)
        }

        fun setPosition(position: Long) {
            MusicNotificationV2.instance.setPosition(position)
        }

        val favour: Boolean
            get() = if (songData != null) service!!.songData!!.getBoolean(Global.KEY_FAVOUR) else false

        fun getPlaying(): Boolean {
            return service!!.playing
        }

        fun getSongData(): JSONObject? {
            return songData
        }

        fun lock(locking: Boolean) {
            lockActivity = locking
        }

        @SuppressLint("WrongConstant")
        fun playOrPause(playing: Boolean) {
            this@PlayServiceV2.playing = playing
            if (mClickListener != null && mClickListener!!.get() != null) mClickListener!!.get()!!
                .playOrPause(playing)
            val options: MutableMap<String, Any> = ArrayMap()
            options[Global.KEY_PLAYING] = playing
            service?.let { invoke(it, Global.KEY_PLAY_OR_PAUSE, options) }
            MusicNotificationV2.instance.playOrPause(playing)
        }

        @SuppressLint("WrongConstant")
        fun favour(favour: Boolean) {
            if (songData != null) {
                songData!![Global.KEY_FAVOUR] = favour
                service?.let { invoke(it, Global.KEY_FAVOUR, songData!!) }
            }
            if (mClickListener != null && mClickListener!!.get() != null) mClickListener!!.get()!!
                .favour(favour)
            MusicNotificationV2.instance.favour(favour)
        }

        @SuppressLint("WrongConstant")
        fun update(option: JSONObject) {
            songData = option
            if (mClickListener != null && mClickListener!!.get() != null) {
                mClickListener!!.get()!!.update(option)
            }
            service?.let { invoke(it, Global.KEY_UPDATE, option) }
            MusicNotificationV2.instance.updateSong(option)
        }

        fun sendMessage(eventName: String, params: Map<String, Any>) {
            if (mEventListener != null && mEventListener!!.get() != null) mEventListener!!.get()!!
                .sendMessage(eventName, params)
        }
    }

    interface OnClickListener {
        fun update(options: JSONObject?)
        fun favour(favour: Boolean)
        fun playOrPause(playing: Boolean)
    }

    interface OnEventListener {
        fun sendMessage(eventName: String, params: Map<String, Any>)
    }

    companion object {
        private var service: PlayServiceV2? = null
        @JvmStatic
        fun startMusicService(context: Context): Intent {
            val intent = Intent(context, PlayServiceV2::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            return intent
        }

        @JvmStatic
        fun stopMusicService(context: Context) {
            context.stopService(Intent(context, PlayServiceV2::class.java))
        }

        @JvmStatic
        operator fun invoke(context: Context, type: String, options: MutableMap<String, Any>) {
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
}