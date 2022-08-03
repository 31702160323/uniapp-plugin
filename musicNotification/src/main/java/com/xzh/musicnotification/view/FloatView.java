package com.xzh.musicnotification.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.utils.Utils;

import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.utils.UniResourceUtils;
import io.dcloud.feature.uniapp.utils.UniViewUtils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class FloatView {
    @SuppressLint("StaticFieldLeak")
    private static volatile FloatView singleton;
    private static WindowManager windowManager;
    private WindowManager.LayoutParams floatLp;
    private View floatView;
    private boolean isShow = false;
    private CountDownTimer timer;
    private String textColor = "";
    private boolean playing;
    private boolean isFavour;
    private boolean showFavour;

    private FloatView() {}

    public static FloatView getInstance() {
        if (singleton == null) {
            synchronized (FloatView.class) {
                if (singleton == null) {
                    singleton = new FloatView();
                }
            }
        }
        return singleton;
    }

    @SuppressLint("InflateParams")
    public void show(AbsSDKInstance instance, String textColor) {
        this.textColor = textColor;
        if (windowManager == null || floatView == null) {
            Context context = instance.getContext();
            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
//            int height = metrics.heightPixels;

            floatLp = new WindowManager.LayoutParams(
                    (int) (width * (1.0f)),
                    UniViewUtils.dip2px(150f),
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?  WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            floatLp.gravity = Gravity.CENTER;
            floatLp.x = 0;
            floatLp.y = 0;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            floatView = inflater.inflate(R.layout.floating_window, null);

            JSONObject data = new JSONObject();
            data.put("message", "更新成功");
            data.put("code", 0);

            floatView.findViewById(R.id.close_view).setOnClickListener(v -> hide());
            floatView.findViewById(R.id.lyric_view).setOnClickListener(v -> hideBackground());
            floatView.findViewById(R.id.next_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_NEXT, data));
            floatView.findViewById(R.id.play_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PAUSE, data));
            floatView.findViewById(R.id.previous_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS, data));

            ApplicationInfo info = Utils.getApplicationInfo(instance.getContext());
            if (info != null) {
                showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR);
                if (showFavour) {
                    floatView.findViewById(R.id.favourite_view).setVisibility(View.VISIBLE);
                    floatView.findViewById(R.id.favourite_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE, data));
                }
            }

            floatView.findViewById(R.id.lyric_view).setOnTouchListener(new View.OnTouchListener() {
                final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatLp;
                double x;
                double y;
                double px;
                double py;
                private boolean isMove;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isMove = false;
                            x = floatWindowLayoutUpdateParam.x;
                            y = floatWindowLayoutUpdateParam.y;
                            px = event.getRawX();
                            py = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                            floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);
                            windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                            if (event.getRawX() - px > 10 || event.getRawY() - py > 10) {
                                isMove = true;
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (timer == null && isMove) {
                                isShow = !isShow;
                                hideBackground();
                            }
                            break;
                    }
                    return isMove;
                }
            });

            windowManager.addView(floatView, floatLp);

            isShow = false;
            hideBackground();
            favour(isFavour);
            playOrPause(playing);
        }
    }

    private void hideBackground() {
        if (windowManager != null && floatView != null) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (isShow) {
                floatView.findViewById(R.id.lyric_view).setBackgroundColor(Color.parseColor("#00000000"));
                floatLp.height = UniViewUtils.dip2px(100f);
            } else {
                floatView.findViewById(R.id.lyric_view).setBackgroundResource(R.drawable.floating_window_bg);
                floatLp.height = UniViewUtils.dip2px(150f);

                timer = new CountDownTimer(5000, 5000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        hideBackground();
                    }
                };
                timer.start();
            }
            floatView.findViewById(R.id.close_view).setVisibility(isShow ? View.GONE : View.VISIBLE);
            floatView.findViewById(R.id.layout_bottom).setVisibility(isShow ? View.GONE : View.VISIBLE);
//        ((TextView) floatView.findViewById(R.id.lyric_view)).setTextColor(Color.parseColor(isShow ? "#FF845EC2" : "#FFFFFFFF"));
            ((TextView) floatView.findViewById(R.id.lyric_view)).setTextColor(isShow ? UniResourceUtils.getColor(textColor, Color.parseColor("#FF845EC2")) : Color.parseColor("#FFFFFFFF"));
            windowManager.updateViewLayout(floatView, floatLp);
            isShow = !isShow;
        }
    }

    public void setLyric(String lyric) {
        if (windowManager != null && floatView != null) {
            ((TextView) floatView.findViewById(R.id.lyric_view)).setText(lyric);
        }
    }

    public void playOrPause(boolean playing) {
        this.playing = playing;
        if (windowManager != null && floatView != null) {
            ((ImageView) floatView.findViewById(R.id.play_view)).setImageResource(playing ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white);
        }
    }

    public void favour(boolean isFavour) {
        this.isFavour = isFavour;
        if (windowManager != null && floatView != null) {
            ((ImageView) floatView.findViewById(R.id.favourite_view)).setImageResource(isFavour ? R.drawable.note_btn_loved : R.drawable.note_btn_love_white);
        }
    }

    public void hide() {
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
            windowManager = null;
            floatView = null;
        }
    }
}
