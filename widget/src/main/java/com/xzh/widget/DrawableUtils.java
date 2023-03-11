package com.xzh.widget;


import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.TypedValue;

import androidx.annotation.IntRange;
import androidx.core.graphics.drawable.DrawableCompat;

public final class DrawableUtils {

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable roundShapeDrawable(int color, @IntRange(from = 0, to = 100) int alpha, int width, int height, float radius) {
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        // 外部矩形的8个半圆角半径，一个圆角由2个半圆角组成
        float[] externalRound = {radius, radius, radius, radius, radius, radius, radius, radius};
        // 内部矩形与外部矩形的距离
        RectF distanceRectF = null;//new RectF(10, 10, 10, 10);
        // 内部矩形的8个半圆角半径值
        float[] insideRound = null;//{10, 10, 10, 10, 10, 10, 10, 10};
        RoundRectShape roundRectShape = new RoundRectShape(externalRound, distanceRectF, insideRound);
        shapeDrawable.setShape(roundRectShape);

        shapeDrawable.setAlpha((int) (alpha * 2.55));
        shapeDrawable.getPaint().setAntiAlias(true);
        shapeDrawable.getPaint().setDither(true);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.setIntrinsicWidth(width);
        shapeDrawable.setIntrinsicHeight(height);
        return shapeDrawable;
    }

    public static int dip2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }
}
