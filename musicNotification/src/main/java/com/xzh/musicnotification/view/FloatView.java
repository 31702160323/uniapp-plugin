package com.xzh.musicnotification.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.Global;
import com.xzh.musicnotification.R;

import io.dcloud.feature.uniapp.AbsSDKInstance;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class FloatView {
    @SuppressLint("StaticFieldLeak")
    private static volatile FloatView singleton;
    private static WindowManager windowManager;
    private View floatView;

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

    public void show(AbsSDKInstance instance) {
        if (windowManager == null || floatView == null) {
            Context context = instance.getContext();
            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
//            int height = metrics.heightPixels;

            WindowManager.LayoutParams floatLp = new WindowManager.LayoutParams(
                    (int) (width * (1.0f)),
                    250,
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
            floatView.findViewById(R.id.previous_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS, data));
            floatView.findViewById(R.id.play_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_PAUSE, data));
            floatView.findViewById(R.id.next_view).setOnClickListener(v -> instance.fireGlobalEventCallback(Global.EVENT_MUSIC_NOTIFICATION_NEXT, data));

            floatView.setOnTouchListener(new View.OnTouchListener() {
                final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatLp;
                double x;
                double y;
                double px;
                double py;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = floatWindowLayoutUpdateParam.x;
                            y = floatWindowLayoutUpdateParam.y;
                            px = event.getRawX();
                            py = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                            floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);
                            windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                            break;
                    }
                    return false;
                }
            });

            windowManager.addView(floatView, floatLp);
        }
    }

    public void update(boolean playing) {
        if (windowManager != null && floatView != null) {
            ((ImageView) floatView.findViewById(R.id.play_view)).setImageResource(playing ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white);
        }
    }

    public void hide() {
        if (windowManager != null) {
            windowManager.removeView(floatView);
            windowManager = null;
            floatView = null;
        }
    }
}
