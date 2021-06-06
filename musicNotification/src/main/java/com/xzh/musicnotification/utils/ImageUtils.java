package com.xzh.musicnotification.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.facebook.common.internal.ByteStreams.copy;

public class ImageUtils {
    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param path 图片路径
     * @return Bitmap
     */
    public static Bitmap GetLocalOrNetBitmap(String path) {
        Bitmap bitmap;
        InputStream in;
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(new URL(path).openStream(), 2 * 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 2 * 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            //第一次采样
            BitmapFactory.Options options = new BitmapFactory.Options();
            //二次采样开始//二次采样时我需要将图片加载出来显示，不能只加载图片的框架，因此inJustDecodeBounds属性要设置为false
            options.inJustDecodeBounds = false;
            //设置缩放比例
            options.inSampleSize = 2;
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            //加载图片并返回
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
