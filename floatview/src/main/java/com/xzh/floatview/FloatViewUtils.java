package com.xzh.floatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

public class FloatViewUtils {
    @SuppressLint("StaticFieldLeak")
    private static volatile FloatViewUtils singleton;
    private final ArrayMap<ViewGroup, WindowManager> windowManagers = new ArrayMap<>();
    private final ArrayMap<ViewGroup, WindowManager.LayoutParams> floatLps = new ArrayMap<>();

    private FloatViewUtils() {}

    public static FloatViewUtils getInstance() {
        if (singleton == null) {
            synchronized (FloatViewUtils.class) {
                if (singleton == null) {
                    singleton = new FloatViewUtils();
                }
            }
        }
        return singleton;
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public void show(Context context, ViewGroup view, int absoluteX, int absoluteY) {
        if (windowManagers.get(view) == null && view != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams floatLp = floatLps.get(view);
            if (floatLp == null) {
                floatLp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        // 全局
                        // Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST,
                        // 应用内
                        WindowManager.LayoutParams.FIRST_SUB_WINDOW,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                floatLp.gravity= Gravity.LEFT | Gravity.TOP;
                floatLp.x = absoluteX;
                floatLp.y = absoluteY;
            }
            View.OnTouchListener touchListener = new View.OnTouchListener() {
                private double lastX = 0.0f;
                private double lastY = 0.0f;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    WindowManager.LayoutParams floatLp = floatLps.get(view);
                    if (floatLp != null) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_MOVE:
                                if (lastX == 0.0f || lastY == 0.0f) {
                                    lastX = event.getRawX();
                                    lastY = event.getRawY();
                                }
                                double nowX = event.getRawX();
                                double nowY = event.getRawY();
                                double movedX = nowX - lastX;
                                double movedY = nowY - lastY;
                                floatLp.x += movedX;
                                floatLp.y += movedY;
                                windowManager.updateViewLayout(view, floatLp);
                                lastX = nowX;
                                lastY = nowY;
                                break;
                            case MotionEvent.ACTION_UP:
                                lastX = 0.0f;
                                lastY = 0.0f;
                                break;
                        }
                    }
                    return true;
                }
            };
            view.setOnTouchListener(touchListener);
            windowManager.addView(view, floatLp);
            floatLps.put(view, floatLp);
            windowManagers.put(view, windowManager);
        }
    }

    public void hide(ViewGroup view) {
        WindowManager windowManager = windowManagers.get(view);
        if (windowManager != null && view != null) {
            windowManager.removeView(view);
            windowManagers.remove(view);
        }
    }
}