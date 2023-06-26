package com.xzh.shortcuts;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

import static com.facebook.common.internal.ByteStreams.copy;

public class ShortcutsModule extends UniModule {
    private final String TAG = this.getClass().getCanonicalName();

    @UniJSMethod(uiThread = false)
    public JSONObject addShortcutsModule(JSONObject options) {
        JSONArray jsonArray = options.getJSONArray("list");
        Map<String, Object> map = new ArrayMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = mWXSDKInstance.getContext().getSystemService(ShortcutManager.class);

            try {
                List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String icon = jsonObject.getString("icon");
                    String path = jsonObject.getString("path");
                    String shortLabel = jsonObject.getString("shortLabel");
                    String title = jsonObject.getString("title");

                    Intent intent = new Intent("io.dcloud.PandoraEntry");
                    intent.setClassName(mWXSDKInstance.getContext(),"io.dcloud.PandoraEntryActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("path", path);

                    if (id == null) {
                        map.put("code", 2);
                        map.put("mag", "id不能为空");
                        return new JSONObject(map);
                    }
                    if (path == null) {
                        map.put("code", 3);
                        map.put("mag", "path不能为空");
                        return new JSONObject(map);
                    }
                    if (title == null) {
                        map.put("code", 4);
                        map.put("mag", "title不能为空");
                        return new JSONObject(map);
                    }
                    if (shortLabel == null || "".equals(shortLabel)) {
                        shortLabel = title;
                    }

                    ShortcutInfo.Builder shortcut = new ShortcutInfo.Builder(mWXSDKInstance.getUIContext(), id)
                            .setShortLabel(shortLabel)
                            .setLongLabel(title)
                            .setIntent(intent);
                    Bitmap bmp = getLocalOrNetBitmap(icon);
                    if (bmp != null) {
                        shortcut.setIcon(Icon.createWithBitmap(bmp));
                    }
                    shortcutInfoList.add(shortcut.build());
                }
                shortcutManager.setDynamicShortcuts(shortcutInfoList);

                map.put("code", 0);
                map.put("mag", "成功");
                return new JSONObject(map);
            } catch (Exception e) {
                e.printStackTrace();
                map.put("code", 7);
                map.put("mag", "未知错误");
                return new JSONObject(map);
            }
        }
        map.put("code", 1);
        map.put("mag", "失败");
        return new JSONObject(map);
    }

    @UniJSMethod(uiThread = false)
    public JSONObject removeAll(){
        Map<String, Object> map = new ArrayMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = mWXSDKInstance.getContext().getSystemService(ShortcutManager.class);

            shortcutManager.removeAllDynamicShortcuts();
            map.put("code", 0);
            map.put("mag", "成功");
            return new JSONObject(map);
        }
        map.put("code", 1);
        map.put("mag", "失败");
        return new JSONObject(map);
    }

    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param url 图片路径
     * @return Bitmap
     */
    private Bitmap getLocalOrNetBitmap(String url) {
        Bitmap bitmap;
        InputStream in;
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 2 * 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 2 * 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "GetLocalOrNetBitmap: " + e.getMessage());
            return null;
        }
    }
}
