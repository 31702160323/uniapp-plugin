package com.xzh.musicnotification.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.xzh.musicnotification.Global
import com.xzh.musicnotification.R
import com.xzh.musicnotification.utils.Utils.dip2px
import com.xzh.musicnotification.utils.Utils.getApplicationInfo
import io.dcloud.feature.uniapp.AbsSDKInstance
import io.dcloud.feature.uniapp.utils.UniViewUtils

class FloatView private constructor() {
    private var floatLp: WindowManager.LayoutParams? = null
    private var isShow = false
    private var timer: CountDownTimer? = null
    private var textColor = ""
    private var playing = false
    private var isFavour = false
    private var floatView: RelativeLayout? = null
    private var lyricView: TextView? = null
    private var closeView: ImageView? = null
    private var layoutBottom: LinearLayout? = null
    private var previousView: ImageView? = null
    private var playView: ImageView? = null
    private var nextView: ImageView? = null
    private var favouriteView: ImageView? = null
    private var showFavour = false
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show(instance: AbsSDKInstance, textColor: String) {
        this.textColor = textColor
        if (windowManager == null || floatView == null) {
            val context = instance.context
            val info = getApplicationInfo(context)
            if (info != null) {
                showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR)
            }
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = context.resources.displayMetrics
            val width = metrics.widthPixels
            floatLp = WindowManager.LayoutParams(
                (width * 1.0f).toInt(),
                UniViewUtils.dip2px(150f),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            floatLp?.let {
                it.gravity = Gravity.CENTER
                it.x = 0
                it.y = 0
            }
            floatView = RelativeLayout(context)
            floatView!!.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            floatView!!.setPadding(dip2px(10f), 0, dip2px(10f), 0)
            lyricView = TextView(context)
            lyricView!!.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            lyricView!!.setPadding(0, dip2px(30f), 0, dip2px(30f))
            lyricView!!.gravity = Gravity.CENTER
            lyricView!!.setTextColor(Color.WHITE)
            lyricView!!.textSize = 16f
            lyricView!!.setBackgroundResource(R.drawable.floating_window_bg)
            floatView!!.addView(lyricView)
            closeView = ImageView(context)
            val closeViewLp = RelativeLayout.LayoutParams(dip2px(15f), dip2px(15f))
            closeViewLp.setMargins(0, dip2px(15f), dip2px(25f), 0)
            closeViewLp.addRule(RelativeLayout.ALIGN_PARENT_END)
            closeView!!.layoutParams = closeViewLp
            closeView!!.scaleType = ImageView.ScaleType.FIT_XY
            closeView!!.setImageResource(R.drawable.note_btn_close)
            floatView!!.addView(closeView)
            layoutBottom = LinearLayout(context)
            val linearLayoutLp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            linearLayoutLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            linearLayoutLp.setMargins(0, dip2px(100f), 0, 0)
            layoutBottom!!.layoutParams = linearLayoutLp
            layoutBottom!!.gravity = Gravity.CENTER
            layoutBottom!!.setPadding(0, dip2px(10f), 0, dip2px(10f))
            if (showFavour) {
                val view = ImageView(context)
                val favouriteViewLp = LinearLayout.LayoutParams(dip2px(25f), dip2px(25f))
                favouriteViewLp.setMargins(dip2px(25f), 0, dip2px(25f), 0)
                view.layoutParams = favouriteViewLp
                view.scaleType = ImageView.ScaleType.FIT_XY
                view.visibility = View.INVISIBLE
                layoutBottom!!.addView(view)
            }
            previousView = ImageView(context)
            val previousViewLp =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(25f))
            previousView!!.layoutParams = previousViewLp
            previousView!!.scaleType = ImageView.ScaleType.FIT_XY
            previousView!!.setImageResource(R.drawable.note_btn_pre_white)
            layoutBottom!!.addView(previousView)
            playView = ImageView(context)
            val playViewLp = LinearLayout.LayoutParams(dip2px(30f), dip2px(30f))
            playViewLp.setMargins(dip2px(25f), 0, dip2px(25f), 0)
            playView!!.layoutParams = playViewLp
            playView!!.scaleType = ImageView.ScaleType.FIT_XY
            playView!!.setImageResource(R.drawable.note_btn_play_white)
            layoutBottom!!.addView(playView)
            nextView = ImageView(context)
            val nextViewLp =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(25f))
            nextView!!.layoutParams = nextViewLp
            nextView!!.scaleType = ImageView.ScaleType.FIT_XY
            nextView!!.setImageResource(R.drawable.note_btn_next_white)
            layoutBottom!!.addView(nextView)
            floatView!!.addView(layoutBottom)
            val data = hashMapOf<String, Any>()
            data["message"] = "更新成功"
            data["code"] = 0
            closeView!!.setOnClickListener { hide() }
            lyricView!!.setOnClickListener { hideBackground() }
            nextView!!.setOnClickListener {
                instance.fireGlobalEventCallback(
                    Global.EVENT_MUSIC_NOTIFICATION_NEXT,
                    data
                )
            }
            playView!!.setOnClickListener {
                instance.fireGlobalEventCallback(
                    Global.EVENT_MUSIC_NOTIFICATION_PAUSE,
                    data
                )
            }
            previousView!!.setOnClickListener {
                instance.fireGlobalEventCallback(
                    Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS,
                    data
                )
            }
            if (showFavour) {
                favouriteView = ImageView(context)
                val favouriteViewLp = LinearLayout.LayoutParams(dip2px(25f), dip2px(25f))
                favouriteViewLp.setMargins(dip2px(25f), 0, dip2px(25f), 0)
                favouriteView!!.layoutParams = favouriteViewLp
                favouriteView!!.scaleType = ImageView.ScaleType.FIT_XY
                favouriteView!!.setImageResource(R.drawable.note_btn_love_white)
                favouriteView!!.setOnClickListener {
                    instance.fireGlobalEventCallback(
                        Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE, data
                    )
                }
                layoutBottom!!.addView(favouriteView)
            }
            lyricView!!.setOnTouchListener(object : OnTouchListener {
                val floatWindowLayoutUpdateParam: WindowManager.LayoutParams = floatLp!!
                var x = 0.0
                var y = 0.0
                var px = 0.0
                var py = 0.0
                private var isMove = false
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isMove = false
                            x = floatWindowLayoutUpdateParam.x.toDouble()
                            y = floatWindowLayoutUpdateParam.y.toDouble()
                            px = event.rawX.toDouble()
                            py = event.rawY.toDouble()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                            floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()
                            windowManager!!.updateViewLayout(
                                floatView,
                                floatWindowLayoutUpdateParam
                            )
                            if (event.rawX - px > 10 || event.rawY - py > 10) {
                                isMove = true
                                if (timer != null) {
                                    timer!!.cancel()
                                    timer = null
                                }
                            }
                        }
                        MotionEvent.ACTION_UP -> if (timer == null && isMove) {
                            isShow = !isShow
                            hideBackground()
                        }
                    }
                    return isMove
                }
            })
            windowManager!!.addView(floatView, floatLp)
            isShow = false
            hideBackground()
            favour(isFavour)
            playOrPause(playing)
        }
    }

    private fun hideBackground() {
        if (windowManager != null && floatView != null) {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
            if (isShow) {
                lyricView!!.setBackgroundColor(Color.parseColor("#00000000"))
                floatLp!!.height = UniViewUtils.dip2px(100f)
            } else {
                lyricView!!.setBackgroundResource(R.drawable.floating_window_bg)
                floatLp!!.height = UniViewUtils.dip2px(150f)
                timer = object : CountDownTimer(5000, 5000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        hideBackground()
                    }
                }
                timer?.start()
            }
            closeView!!.visibility = if (isShow) View.GONE else View.VISIBLE
            layoutBottom!!.visibility = if (isShow) View.GONE else View.VISIBLE
            lyricView!!.setTextColor(Color.parseColor(if (isShow) textColor else "#FFFFFFFF"))
            windowManager!!.updateViewLayout(floatView, floatLp)
            isShow = !isShow
        }
    }

    fun setLyric(lyric: String?) {
        if (windowManager != null && floatView != null) {
//            ((TextView) floatView.findViewById(R.id.lyric_view)).setText(lyric);
            lyricView!!.text = lyric
        }
    }

    fun playOrPause(playing: Boolean) {
        this.playing = playing
        if (windowManager != null && floatView != null) {
            playView!!.setImageResource(if (playing) R.drawable.note_btn_pause_white else R.drawable.note_btn_play_white)
        }
    }

    fun favour(isFavour: Boolean) {
        this.isFavour = isFavour
        if (windowManager != null && floatView != null) {
            favouriteView!!.setImageResource(if (isFavour) R.drawable.note_btn_loved else R.drawable.note_btn_love_white)
        }
    }

    fun hide() {
        if (windowManager != null && floatView != null) {
            windowManager!!.removeView(floatView)
            windowManager = null
            floatView = null
            lyricView = null
            closeView = null
            layoutBottom = null
            previousView = null
            playView = null
            nextView = null
            favouriteView = null
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: FloatView? = null
        private var windowManager: WindowManager? = null
        val instance: FloatView?
            get() {
                if (singleton == null) {
                    synchronized(FloatView::class.java) {
                        if (singleton == null) {
                            singleton = FloatView()
                        }
                    }
                }
                return singleton
            }
    }
}