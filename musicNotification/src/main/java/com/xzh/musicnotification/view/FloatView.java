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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;
import com.xzh.musicnotification.utils.Utils;

import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.utils.UniViewUtils;

import static android.content.Context.WINDOW_SERVICE;

public class FloatView {
    @SuppressLint("StaticFieldLeak")
    private static volatile FloatView singleton;
    private static WindowManager windowManager;
    private WindowManager.LayoutParams floatLp;
    private boolean isShow = false;
    private CountDownTimer timer;
    private String textColor = "";
    private boolean playing;
    private boolean isFavour;
    private RelativeLayout floatView;
    private TextView lyricView;
    private ImageView closeView;
    private LinearLayout layoutBottom;
    private ImageView previousView;
    private ImageView playView;
    private ImageView nextView;
    private ImageView favouriteView;
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

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public void show(AbsSDKInstance instance, String textColor) {
        this.textColor = textColor;
        if (windowManager == null || floatView == null) {
            Context context = instance.getContext();

            ApplicationInfo info = Utils.getApplicationInfo(context);
            if (info != null) {
                showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR);
            }

            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;

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

            floatView = new RelativeLayout(context);
            floatView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            floatView.setPadding(Utils.dip2px(10), 0, Utils.dip2px(10), 0);

            lyricView = new TextView(context);
            lyricView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            lyricView.setPadding(0, Utils.dip2px(30), 0, Utils.dip2px(30));
            lyricView.setGravity(Gravity.CENTER);
            lyricView.setTextColor(Color.WHITE);
            lyricView.setTextSize(16);
            lyricView.setBackgroundResource(R.drawable.floating_window_bg);
            floatView.addView(lyricView);

            closeView = new ImageView(context);
            RelativeLayout.LayoutParams closeViewLp = new RelativeLayout.LayoutParams(Utils.dip2px(15), Utils.dip2px(15));
            closeViewLp.setMargins(0, Utils.dip2px(15), Utils.dip2px(25), 0);
            closeViewLp.addRule(RelativeLayout.ALIGN_PARENT_END);
            closeView.setLayoutParams(closeViewLp);
            closeView.setScaleType(ImageView.ScaleType.FIT_XY);
            closeView.setImageResource(R.drawable.note_btn_close);
            floatView.addView(closeView);

            layoutBottom = new LinearLayout(context);
            RelativeLayout.LayoutParams linearLayoutLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            linearLayoutLp.setMargins(0, Utils.dip2px(100), 0, 0);
            layoutBottom.setLayoutParams(linearLayoutLp);
            layoutBottom.setGravity(Gravity.CENTER);
            layoutBottom.setPadding(0, Utils.dip2px(10), 0, Utils.dip2px(10));

            if (showFavour) {
                ImageView view = new ImageView(context);
                LinearLayout.LayoutParams favouriteViewLp = new LinearLayout.LayoutParams(Utils.dip2px(25), Utils.dip2px(25));
                favouriteViewLp.setMargins(Utils.dip2px(25), 0, Utils.dip2px(25), 0);
                view.setLayoutParams(favouriteViewLp);
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                view.setVisibility(View.INVISIBLE);
                layoutBottom.addView(view);
            }

            previousView = new ImageView(context);
            LinearLayout.LayoutParams previousViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, Utils.dip2px(25));
            previousView.setLayoutParams(previousViewLp);
            previousView.setScaleType(ImageView.ScaleType.FIT_XY);
            previousView.setImageResource(R.drawable.note_btn_pre_white);
            layoutBottom.addView(previousView);

            playView = new ImageView(context);
            LinearLayout.LayoutParams playViewLp = new LinearLayout.LayoutParams(Utils.dip2px(30), Utils.dip2px(30));
            playViewLp.setMargins(Utils.dip2px(25), 0, Utils.dip2px(25), 0);
            playView.setLayoutParams(playViewLp);
            playView.setScaleType(ImageView.ScaleType.FIT_XY);
            playView.setImageResource(R.drawable.note_btn_play_white);
            layoutBottom.addView(playView);

            nextView = new ImageView(context);
            LinearLayout.LayoutParams nextViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, Utils.dip2px(25));
            nextView.setLayoutParams(nextViewLp);
            nextView.setScaleType(ImageView.ScaleType.FIT_XY);
            nextView.setImageResource(R.drawable.note_btn_next_white);
            layoutBottom.addView(nextView);

            floatView.addView(layoutBottom);

            JSONObject data = new JSONObject();
            data.put("message", "更新成功");
            data.put("code", 0);

            closeView.setOnClickListener(v -> hide());
            lyricView.setOnClickListener(v -> hideBackground());
            nextView.setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_NEXT, data));
            playView.setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PAUSE, data));
            previousView.setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS, data));

            if (showFavour) {
                favouriteView = new ImageView(context);
                LinearLayout.LayoutParams favouriteViewLp = new LinearLayout.LayoutParams(Utils.dip2px(25), Utils.dip2px(25));
                favouriteViewLp.setMargins(Utils.dip2px(25), 0, Utils.dip2px(25), 0);
                favouriteView.setLayoutParams(favouriteViewLp);
                favouriteView.setScaleType(ImageView.ScaleType.FIT_XY);
                favouriteView.setImageResource(R.drawable.note_btn_love_white);
                favouriteView.setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE, data));
                layoutBottom.addView(favouriteView);
            }
            lyricView.setOnTouchListener(new View.OnTouchListener() {
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
                lyricView.setBackgroundColor(Color.parseColor("#00000000"));
                floatLp.height = UniViewUtils.dip2px(100f);
            } else {
                lyricView.setBackgroundResource(R.drawable.floating_window_bg);
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
            closeView.setVisibility(isShow ? View.GONE : View.VISIBLE);
            layoutBottom.setVisibility(isShow ? View.GONE : View.VISIBLE);
            lyricView.setTextColor(Color.parseColor(isShow ? textColor : "#FFFFFFFF"));

            windowManager.updateViewLayout(floatView, floatLp);
            isShow = !isShow;
        }
    }

    public void setLyric(String lyric) {
        if (windowManager != null && floatView != null) {
//            ((TextView) floatView.findViewById(R.id.lyric_view)).setText(lyric);
            lyricView.setText(lyric);
        }
    }

    public void playOrPause(boolean playing) {
        this.playing = playing;
        if (windowManager != null && floatView != null) {
            playView.setImageResource(playing ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white);
        }
    }

    public void favour(boolean isFavour) {
        this.isFavour = isFavour;
        if (windowManager != null && floatView != null) {
            favouriteView.setImageResource(isFavour ? R.drawable.note_btn_loved : R.drawable.note_btn_love_white);
        }
    }

    public void hide() {
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
            windowManager = null;
            floatView = null;
            lyricView = null;
            closeView = null;
            layoutBottom = null;
            previousView = null;
            playView = null;
            nextView = null;
            favouriteView = null;
        }
    }
}
