package com.xzh.musicnotification

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.xzh.musicnotification.service.NotificationReceiver
import com.xzh.musicnotification.service.PlayServiceV2
import com.xzh.musicnotification.utils.Utils
import com.xzh.musicnotification.view.SlidingFinishLayout
import com.xzh.musicnotification.view.SlidingFinishLayout.OnSlidingFinishListener
import kotlin.math.max


class LockActivityV2 : AppCompatActivity(), OnSlidingFinishListener, View.OnClickListener {
    private var mWidth = 0
    private var mHeight = 0
    private var tvAudio: TextView? = null
    private var tvAudioName: TextView? = null
    private var lockDate: ImageView? = null
    private var playView: ImageView? = null
    private var favouriteView: ImageView? = null
    private var connection: ServiceConnection? = null
    private var mBinder: IMusicServiceAidlInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.fullScreen(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
//            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        setContentView(R.layout.activity_lock)
        initView()
        val windowManager = this.windowManager
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        mWidth = displayMetrics.widthPixels
        mHeight = displayMetrics.heightPixels
        connection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                mBinder = IMusicServiceAidlInterface.Stub.asInterface(iBinder)
                mBinder?.setActivityEventListener(object : IMusicActivityCallbackAidlInterface.Stub() {
                    override fun update(option: MutableMap<Any?, Any?>?) {
                        this@LockActivityV2.update(option)
                    }

                    override fun setFavour(favour: Boolean) {
                        this@LockActivityV2.favour(favour)
                    }

                    override fun playOrPause(playing: Boolean) {
                        this@LockActivityV2.playOrPause(playing)
                    }
                })
                if (isUiThread()) {
                    this@LockActivityV2.update(mBinder?.songData)
                } else {
                    runOnUiThread { this@LockActivityV2.update(mBinder?.songData) }
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                mBinder = null
            }
        }
        bindService(
            Intent(this, PlayServiceV2::class.java),
            connection as ServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun initView() {
        val vLockRoot = findViewById<SlidingFinishLayout>(R.id.lock_root)
        vLockRoot.setOnSlidingFinishListener(this)
        lockDate = findViewById(R.id.iv_audio)
        tvAudioName = findViewById(R.id.tv_audio_name)
        tvAudio = findViewById(R.id.tv_audio)
        favouriteView = findViewById(R.id.favourite_view)
        val info = Utils.getApplicationInfo(this)
        if (info != null) {
            val showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR)
            if (showFavour) favouriteView?.visibility = View.VISIBLE
        }
        playView = findViewById(R.id.play_view)
        favouriteView?.setOnClickListener(this)
        playView?.setOnClickListener(this)
        findViewById<View>(R.id.previous_view).setOnClickListener(this)
        findViewById<View>(R.id.next_view).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (mBinder == null) return
        var extraType = ""
        var eventName = Global.EVENT_MUSIC_NOTIFICATION_ERROR
        val data = hashMapOf<String, Any>()
        data["message"] = "更新锁屏页成功"
        data["code"] = 0
        val viewId = view.id
        val ids =
            intArrayOf(R.id.previous_view, R.id.next_view, R.id.favourite_view, R.id.play_view)
        val extras = arrayOf(
            NotificationReceiver.EXTRA_PRE,
            NotificationReceiver.EXTRA_NEXT,
            NotificationReceiver.EXTRA_FAV,
            NotificationReceiver.EXTRA_PLAY
        )
        for (i in ids.indices) {
            if (viewId != ids[i]) continue
            extraType = extras[i]
        }
        when (extraType) {
            NotificationReceiver.EXTRA_PRE -> eventName = Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS
            NotificationReceiver.EXTRA_NEXT -> eventName = Global.EVENT_MUSIC_NOTIFICATION_NEXT
            NotificationReceiver.EXTRA_FAV -> {
                mBinder?.favour = !mBinder?.favour!!
                eventName = Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE
            }
            NotificationReceiver.EXTRA_PLAY -> {
                mBinder?.playOrPause(!mBinder?.playing!!)
                eventName = Global.EVENT_MUSIC_NOTIFICATION_PAUSE
            }
            else -> {
                data["message"] = "更新锁屏页失败"
                data["code"] = -6
            }
        }
        mBinder?.sendMessage(eventName, data)
    }

    /**
     * 重写物理返回键，使不能回退
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    override fun onDestroy() {
        unbindService(connection!!)
        super.onDestroy()
    }

    /**
     * 滑动销毁锁屏页面
     */
    override fun onSlidingFinish() {
        finish()
    }

    fun playOrPause(playing: Boolean) {
        if (playing) {
            playView!!.setImageResource(R.drawable.note_btn_pause_white)
        } else {
            playView!!.setImageResource(R.drawable.note_btn_play_white)
        }
    }

    fun favour(favour: Boolean) {
        if (favour) {
            favouriteView!!.setImageResource(R.drawable.note_btn_loved)
        } else {
            favouriteView!!.setImageResource(R.drawable.note_btn_love_white)
        }
    }

    fun update(options: MutableMap<Any?, Any?>?) {
        if (options == null) return
        if (isUiThread()) {
            updateUI(options)
        } else {
            runOnUiThread { updateUI(options) }
        }
    }

    private fun updateUI(options: MutableMap<Any?, Any?>) {
        mBinder?.favour?.let { favour(it) }
        mBinder?.playing?.let { playOrPause(it) }
        if (options[Global.KEY_SONG_NAME] != null) {
            tvAudioName!!.text = options[Global.KEY_SONG_NAME].toString()
        }
        if (options[Global.KEY_ARTISTS_NAME] != null) {
            tvAudio!!.text = options[Global.KEY_ARTISTS_NAME].toString()
        }
        updatePicUrl(options["picUrl"]?.toString())
    }

    private fun updatePicUrl(picUrl: String?) {
        if (picUrl == null) return
        Glide.with(this.applicationContext)
            .asBitmap()
            .load(picUrl)
            .sizeMultiplier(0.8f)
            .override(mWidth, mHeight)
            .format(DecodeFormat.PREFER_RGB_565)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    if (isUiThread()) {
                        lockDate!!.setImageBitmap(changeAlpha(resource))
                    } else {
                        runOnUiThread { lockDate!!.setImageBitmap(changeAlpha(resource)) }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    fun changeAlpha(bitmap: Bitmap): Bitmap {
        return try {
            val w = bitmap.width
            val h = bitmap.height
            val height = mHeight * w / mWidth
            val result = Bitmap.createBitmap(w, height, Bitmap.Config.ARGB_8888)
            var r: Int
            var g: Int
            var b: Int
            var a: Int
            var color: Int
            val oldPx = IntArray(w * h)
            val newPx = IntArray(w * height)
            bitmap.getPixels(oldPx, 0, w, 0, 0, w, h)
            lockDate!!.setBackgroundColor(oldPx[100])
            for (x in 0 until w) {
                for (y in 0 until height) {
                    if (y < h) {
                        color = oldPx[x + w * y]
                        a = if (w / h - y > 255) {
                            Color.alpha(color)
                        } else {
                            h - y
                        }
                    } else {
                        color = oldPx[100]
                        a = Color.alpha(color)
                    }
                    r = Color.red(color)
                    g = Color.green(color)
                    b = Color.blue(color)
                    a = if (a > 255) 255 else max(a, 0)
                    newPx[x + w * y] = Color.argb(a, r, g, b)
                }
            }
            result.setPixels(newPx, 0, w, 0, 0, w, height)
            result
        } catch (e: Exception) {
            bitmap
        }
    }

    fun isUiThread(): Boolean {
        return Thread.currentThread().id == Looper.getMainLooper().thread.id
    }
}